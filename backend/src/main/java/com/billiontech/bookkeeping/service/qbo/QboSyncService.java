package com.billiontech.bookkeeping.service.qbo;

import com.billiontech.bookkeeping.entity.AgentJob;
import com.billiontech.bookkeeping.entity.Client;
import com.billiontech.bookkeeping.entity.GlAccount;
import com.billiontech.bookkeeping.entity.Transaction;
import com.billiontech.bookkeeping.repository.AgentJobRepository;
import com.billiontech.bookkeeping.repository.ClientRepository;
import com.billiontech.bookkeeping.repository.GlAccountRepository;
import com.billiontech.bookkeeping.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.ipp.core.Context;
import com.intuit.ipp.core.ServiceType;
import com.intuit.ipp.data.Account;
import com.intuit.ipp.data.Bill;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.JournalEntry;
import com.intuit.ipp.data.Payment;
import com.intuit.ipp.data.Purchase;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.security.OAuth2Authorizer;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class QboSyncService {

    private static final Logger log = LoggerFactory.getLogger(QboSyncService.class);

    private final QboTokenService qboTokenService;
    private final ClientRepository clientRepository;
    private final GlAccountRepository glAccountRepository;
    private final TransactionRepository transactionRepository;
    private final AgentJobRepository agentJobRepository;
    private final ObjectMapper objectMapper;

    public QboSyncService(QboTokenService qboTokenService,
                          ClientRepository clientRepository,
                          GlAccountRepository glAccountRepository,
                          TransactionRepository transactionRepository,
                          AgentJobRepository agentJobRepository,
                          ObjectMapper objectMapper) {
        this.qboTokenService = qboTokenService;
        this.clientRepository = clientRepository;
        this.glAccountRepository = glAccountRepository;
        this.transactionRepository = transactionRepository;
        this.agentJobRepository = agentJobRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public int syncChartOfAccounts(UUID clientId) throws FMSException {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new QboOAuthService.QboException("Client not found: " + clientId));

        DataService dataService = createDataService(clientId, client.getQboRealmId());
        QueryResult result = dataService.executeQuery("SELECT * FROM Account WHERE Active = true");

        @SuppressWarnings("unchecked")
        List<Account> accounts = (List<Account>) result.getEntities();
        int count = 0;

        if (accounts != null) {
            for (Account acct : accounts) {
                GlAccount glAccount = glAccountRepository
                        .findByClientIdAndQboAccountId(clientId, acct.getId())
                        .orElseGet(() -> {
                            GlAccount newAcct = new GlAccount();
                            newAcct.setClient(client);
                            newAcct.setQboAccountId(acct.getId());
                            return newAcct;
                        });

                glAccount.setName(acct.getName());
                glAccount.setAccountType(
                        acct.getAccountType() != null ? acct.getAccountType().value() : null);
                glAccount.setAccountSubType(acct.getAccountSubType());
                glAccount.setActive(acct.isActive());
                glAccountRepository.save(glAccount);
                count++;
            }
        }

        log.info("Synced {} chart of accounts for client={}", count, clientId);
        return count;
    }

    @Transactional
    public int syncTransactions(UUID clientId, LocalDate fromDate, LocalDate toDate)
            throws FMSException {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new QboOAuthService.QboException("Client not found: " + clientId));

        DataService dataService = createDataService(clientId, client.getQboRealmId());
        String dateRange = String.format("'%s' AND '%s'", fromDate, toDate);
        int count = 0;

        count += syncEntityType(dataService, client, "Bill", "BILL",
                "SELECT * FROM Bill WHERE TxnDate >= " + dateRange);
        count += syncEntityType(dataService, client, "Invoice", "INVOICE",
                "SELECT * FROM Invoice WHERE TxnDate >= " + dateRange);
        count += syncEntityType(dataService, client, "Payment", "PAYMENT",
                "SELECT * FROM Payment WHERE TxnDate >= " + dateRange);
        count += syncEntityType(dataService, client, "JournalEntry", "JOURNAL",
                "SELECT * FROM JournalEntry WHERE TxnDate >= " + dateRange);
        count += syncEntityType(dataService, client, "Purchase", "BANK_TXN",
                "SELECT * FROM Purchase WHERE TxnDate >= " + dateRange);

        log.info("Synced {} transactions for client={} ({} to {})", count, clientId, fromDate, toDate);
        return count;
    }

    private int syncEntityType(DataService dataService, Client client,
                               String entityName, String txnType, String query)
            throws FMSException {
        QueryResult result = dataService.executeQuery(query);
        List<?> entities = result.getEntities();
        if (entities == null) return 0;

        int count = 0;
        for (Object entity : entities) {
            upsertTransaction(client, entity, txnType);
            count++;
        }
        log.debug("Synced {} {} records for client={}", count, entityName, client.getId());
        return count;
    }

    private void upsertTransaction(Client client, Object entity, String txnType) {
        String qboId = extractId(entity);
        BigDecimal totalAmt = extractTotalAmt(entity);
        String vendorCustomer = extractVendorCustomer(entity, txnType);
        String description = extractDescription(entity);
        LocalDate txnDate = extractTxnDate(entity);
        String rawPayload = serializeToJson(entity);

        Transaction txn = transactionRepository
                .findByClientIdAndQboTxnId(client.getId(), qboId)
                .orElseGet(() -> {
                    Transaction newTxn = new Transaction();
                    newTxn.setClient(client);
                    newTxn.setQboTxnId(qboId);
                    return newTxn;
                });

        txn.setTxnDate(txnDate != null ? txnDate : LocalDate.now());
        txn.setType(txnType);
        txn.setAmount(totalAmt != null ? totalAmt : BigDecimal.ZERO);
        txn.setVendorCustomer(vendorCustomer);
        txn.setDescription(description);
        txn.setRawPayload(rawPayload);

        // Do NOT overwrite coding fields if already set
        // (preserves manual overrides and AI-coded values)

        transactionRepository.save(txn);
    }

    /** Runs sync asynchronously and updates the job record with progress. */
    @Async
    public void executeSyncJob(UUID jobId, UUID clientId, LocalDate fromDate, LocalDate toDate) {
        AgentJob job = agentJobRepository.findById(jobId).orElse(null);
        if (job == null) return;

        job.setStatus("RUNNING");
        job.setStartedAt(OffsetDateTime.now());
        agentJobRepository.save(job);

        try {
            int accountCount = syncChartOfAccounts(clientId);
            int txnCount = syncTransactions(clientId, fromDate, toDate);

            job.setStatus("DONE");
            job.setResultSummary(String.format(
                    "{\"accountsSynced\":%d,\"transactionsSynced\":%d}", accountCount, txnCount));
        } catch (Exception e) {
            log.error("Sync job {} failed for client={}: {}", jobId, clientId, e.getMessage(), e);
            job.setStatus("FAILED");
            job.setResultSummary(String.format("{\"error\":\"%s\"}",
                    e.getMessage().replace("\"", "\\\"").replace("\n", " ")));
        } finally {
            job.setCompletedAt(OffsetDateTime.now());
            agentJobRepository.save(job);
        }
    }

    private DataService createDataService(UUID clientId, String realmId) throws FMSException {
        String accessToken = qboTokenService.getAccessToken(clientId);
        OAuth2Authorizer authorizer = new OAuth2Authorizer(accessToken);
        Context context = new Context(authorizer, ServiceType.QBO, realmId);
        return new DataService(context);
    }

    private String extractId(Object entity) {
        if (entity instanceof com.intuit.ipp.data.IntuitEntity ie) return ie.getId();
        return null;
    }

    private BigDecimal extractTotalAmt(Object entity) {
        if (entity instanceof Bill b) return b.getTotalAmt();
        if (entity instanceof Invoice i) return i.getTotalAmt();
        if (entity instanceof Payment p) return p.getTotalAmt();
        if (entity instanceof JournalEntry j) return j.getTotalAmt();
        if (entity instanceof Purchase pu) return pu.getTotalAmt();
        return BigDecimal.ZERO;
    }

    private String extractVendorCustomer(Object entity, String txnType) {
        ReferenceType ref = null;
        if (entity instanceof Bill b) ref = b.getVendorRef();
        else if (entity instanceof Invoice i) ref = i.getCustomerRef();
        else if (entity instanceof Payment p) ref = p.getCustomerRef();
        else if (entity instanceof Purchase pu) ref = pu.getEntityRef();
        return ref != null ? ref.getName() : null;
    }

    private String extractDescription(Object entity) {
        if (entity instanceof com.intuit.ipp.data.Transaction txn) {
            if (txn.getPrivateNote() != null) return txn.getPrivateNote();
            return txn.getDocNumber();
        }
        return null;
    }

    private LocalDate extractTxnDate(Object entity) {
        if (entity instanceof com.intuit.ipp.data.Transaction txn && txn.getTxnDate() != null) {
            Date date = txn.getTxnDate();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private String serializeToJson(Object entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize QBO entity to JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
