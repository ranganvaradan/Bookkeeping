package com.billiontech.bookkeeping.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"client_id", "qbo_txn_id"})
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "qbo_txn_id", length = 100)
    private String qboTxnId;

    @Column(name = "txn_date", nullable = false)
    private LocalDate txnDate;

    @Column(length = 30)
    private String type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "vendor_customer")
    private String vendorCustomer;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "gl_account_code", length = 100)
    private String glAccountCode;

    @Column(name = "gl_account_name")
    private String glAccountName;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Column(name = "coding_confidence", precision = 4, scale = 3)
    private BigDecimal codingConfidence;

    @Column(name = "coding_status", length = 20)
    private String codingStatus;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public String getQboTxnId() { return qboTxnId; }
    public void setQboTxnId(String qboTxnId) { this.qboTxnId = qboTxnId; }

    public LocalDate getTxnDate() { return txnDate; }
    public void setTxnDate(LocalDate txnDate) { this.txnDate = txnDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getVendorCustomer() { return vendorCustomer; }
    public void setVendorCustomer(String vendorCustomer) { this.vendorCustomer = vendorCustomer; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGlAccountCode() { return glAccountCode; }
    public void setGlAccountCode(String glAccountCode) { this.glAccountCode = glAccountCode; }

    public String getGlAccountName() { return glAccountName; }
    public void setGlAccountName(String glAccountName) { this.glAccountName = glAccountName; }

    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }

    public BigDecimal getCodingConfidence() { return codingConfidence; }
    public void setCodingConfidence(BigDecimal codingConfidence) { this.codingConfidence = codingConfidence; }

    public String getCodingStatus() { return codingStatus; }
    public void setCodingStatus(String codingStatus) { this.codingStatus = codingStatus; }

    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
