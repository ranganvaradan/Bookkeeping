package com.billiontech.bookkeeping.controller;

import com.billiontech.bookkeeping.controller.dto.JobResponse;
import com.billiontech.bookkeeping.controller.dto.SyncRequest;
import com.billiontech.bookkeeping.controller.dto.SyncResponse;
import com.billiontech.bookkeeping.entity.AgentJob;
import com.billiontech.bookkeeping.entity.Client;
import com.billiontech.bookkeeping.repository.AgentJobRepository;
import com.billiontech.bookkeeping.repository.ClientRepository;
import com.billiontech.bookkeeping.security.TenantContext;
import com.billiontech.bookkeeping.service.qbo.QboOAuthService;
import com.billiontech.bookkeeping.service.qbo.QboSyncService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SyncController {

    private final QboSyncService qboSyncService;
    private final ClientRepository clientRepository;
    private final AgentJobRepository agentJobRepository;

    public SyncController(QboSyncService qboSyncService,
                          ClientRepository clientRepository,
                          AgentJobRepository agentJobRepository) {
        this.qboSyncService = qboSyncService;
        this.clientRepository = clientRepository;
        this.agentJobRepository = agentJobRepository;
    }

    @PostMapping("/clients/{clientId}/sync")
    public ResponseEntity<SyncResponse> sync(@PathVariable UUID clientId,
                                             @Valid @RequestBody SyncRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new QboOAuthService.QboException("Client not found"));

        if (!client.getTenant().getId().equals(tenantId)) {
            return ResponseEntity.status(403).build();
        }

        if (client.getQboRealmId() == null) {
            throw new QboOAuthService.QboException("Client is not connected to QuickBooks");
        }

        AgentJob job = new AgentJob();
        job.setClient(client);
        job.setJobType("QBO_SYNC");
        job.setStatus("PENDING");
        agentJobRepository.save(job);

        qboSyncService.executeSyncJob(job.getId(), clientId,
                request.getFromDate(), request.getToDate());

        return ResponseEntity.ok(new SyncResponse(job.getId()));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID jobId) {
        AgentJob job = agentJobRepository.findById(jobId)
                .orElseThrow(() -> new QboOAuthService.QboException("Job not found"));

        return ResponseEntity.ok(JobResponse.from(job));
    }
}
