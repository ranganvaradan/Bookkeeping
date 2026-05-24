package com.billiontech.bookkeeping.controller;

import com.billiontech.bookkeeping.controller.dto.ClientResponse;
import com.billiontech.bookkeeping.repository.ClientRepository;
import com.billiontech.bookkeeping.security.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepository clientRepository;

    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> listClients() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        List<ClientResponse> clients = clientRepository.findByTenantId(tenantId)
                .stream()
                .map(c -> new ClientResponse(c.getId(), c.getName(), c.getEntityType(), c.getTenant().getId()))
                .toList();

        return ResponseEntity.ok(clients);
    }
}
