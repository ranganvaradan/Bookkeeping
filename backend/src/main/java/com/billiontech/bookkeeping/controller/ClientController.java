package com.billiontech.bookkeeping.controller;

import com.billiontech.bookkeeping.controller.dto.ClientResponse;
import com.billiontech.bookkeeping.controller.dto.CreateClientRequest;
import com.billiontech.bookkeeping.entity.AgentJob;
import com.billiontech.bookkeeping.entity.Client;
import com.billiontech.bookkeeping.entity.Tenant;
import com.billiontech.bookkeeping.repository.AgentJobRepository;
import com.billiontech.bookkeeping.repository.ClientRepository;
import com.billiontech.bookkeeping.repository.TenantRepository;
import com.billiontech.bookkeeping.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepository clientRepository;
    private final TenantRepository tenantRepository;
    private final AgentJobRepository agentJobRepository;

    public ClientController(ClientRepository clientRepository,
                            TenantRepository tenantRepository,
                            AgentJobRepository agentJobRepository) {
        this.clientRepository = clientRepository;
        this.tenantRepository = tenantRepository;
        this.agentJobRepository = agentJobRepository;
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> listClients() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        List<ClientResponse> clients = clientRepository.findByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        return clientRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(tenantId))
                .map(c -> ResponseEntity.ok(toResponse(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody CreateClientRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found"));

        Client client = new Client();
        client.setTenant(tenant);
        client.setName(request.name());
        client.setEntityType(request.entityType());
        client = clientRepository.save(client);

        return ResponseEntity.status(201).body(toResponse(client));
    }

    private ClientResponse toResponse(Client client) {
        OffsetDateTime lastSyncAt = agentJobRepository
                .findTopByClient_IdAndStatusAndJobTypeOrderByCompletedAtDesc(
                        client.getId(), "DONE", "QBO_SYNC")
                .map(AgentJob::getCompletedAt)
                .orElse(null);

        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getEntityType(),
                client.getTenant().getId(),
                client.getQboRealmId(),
                lastSyncAt
        );
    }
}
