package com.billiontech.bookkeeping.controller;

import com.billiontech.bookkeeping.entity.Client;
import com.billiontech.bookkeeping.repository.ClientRepository;
import com.billiontech.bookkeeping.security.TenantContext;
import com.billiontech.bookkeeping.service.qbo.QboOAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class QboController {

    private final QboOAuthService qboOAuthService;
    private final ClientRepository clientRepository;

    public QboController(QboOAuthService qboOAuthService, ClientRepository clientRepository) {
        this.qboOAuthService = qboOAuthService;
        this.clientRepository = clientRepository;
    }

    @GetMapping("/clients/{clientId}/qbo/connect")
    public ResponseEntity<Map<String, String>> connect(@PathVariable UUID clientId) {
        UUID tenantId = TenantContext.getTenantId();

        // Verify the client belongs to the authenticated user's tenant
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new QboOAuthService.QboException("Client not found"));

        if (!client.getTenant().getId().equals(tenantId)) {
            return ResponseEntity.status(403).build();
        }

        String authUrl = qboOAuthService.generateAuthUrl(clientId);
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    /**
     * QBO redirects here after the user authorizes. This endpoint is public
     * because the redirect comes from Intuit's servers, not from the frontend.
     */
    @GetMapping("/qbo/callback")
    public RedirectView callback(@RequestParam("code") String code,
                                 @RequestParam("realmId") String realmId,
                                 @RequestParam("state") String state) {
        UUID clientId = qboOAuthService.handleCallback(code, realmId, state);
        return new RedirectView("/clients/" + clientId + "?connected=true");
    }
}
