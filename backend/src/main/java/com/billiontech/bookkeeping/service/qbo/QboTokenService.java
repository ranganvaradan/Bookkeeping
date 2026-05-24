package com.billiontech.bookkeeping.service.qbo;

import com.billiontech.bookkeeping.config.QboConfig;
import com.billiontech.bookkeeping.entity.Client;
import com.billiontech.bookkeeping.repository.ClientRepository;
import com.billiontech.bookkeeping.util.AesEncryptionUtil;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class QboTokenService {

    private static final Logger log = LoggerFactory.getLogger(QboTokenService.class);
    private static final int REFRESH_WINDOW_MINUTES = 60;

    private final ClientRepository clientRepository;
    private final QboOAuthService qboOAuthService;
    private final QboConfig qboConfig;

    public QboTokenService(ClientRepository clientRepository,
                           QboOAuthService qboOAuthService,
                           QboConfig qboConfig) {
        this.clientRepository = clientRepository;
        this.qboOAuthService = qboOAuthService;
        this.qboConfig = qboConfig;
    }

    public String getAccessToken(UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new QboOAuthService.QboException("Client not found: " + clientId));

        if (client.getQboAccessToken() == null) {
            throw new QboOAuthService.QboException("Client is not connected to QBO: " + clientId);
        }

        return AesEncryptionUtil.decrypt(client.getQboAccessToken(), qboConfig.getEncryptionKey());
    }

    @Transactional
    public void refreshTokenIfNeeded(UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new QboOAuthService.QboException("Client not found: " + clientId));

        if (client.getQboRefreshToken() == null || client.getTokenExpiresAt() == null) {
            return;
        }

        // Only refresh if token expires within the refresh window
        if (client.getTokenExpiresAt().isAfter(
                OffsetDateTime.now().plusMinutes(REFRESH_WINDOW_MINUTES))) {
            return;
        }

        try {
            String encKey = qboConfig.getEncryptionKey();
            String decryptedRefresh = AesEncryptionUtil.decrypt(client.getQboRefreshToken(), encKey);

            BearerTokenResponse tokenResponse =
                    qboOAuthService.getOauthClient().refreshToken(decryptedRefresh);

            client.setQboAccessToken(AesEncryptionUtil.encrypt(tokenResponse.getAccessToken(), encKey));
            client.setQboRefreshToken(AesEncryptionUtil.encrypt(tokenResponse.getRefreshToken(), encKey));
            client.setTokenExpiresAt(
                    OffsetDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));

            clientRepository.save(client);
            log.info("Refreshed QBO tokens for client={}", clientId);
        } catch (OAuthException e) {
            log.error("Failed to refresh QBO token for client={}: {}", clientId, e.getMessage());
        }
    }

    /** Runs every 30 minutes to proactively refresh tokens nearing expiry. */
    @Scheduled(fixedDelay = 1800000)
    public void scheduledTokenRefresh() {
        List<Client> connectedClients = clientRepository.findByQboRealmIdIsNotNull();
        log.debug("Checking {} connected QBO clients for token refresh", connectedClients.size());

        for (Client client : connectedClients) {
            try {
                refreshTokenIfNeeded(client.getId());
            } catch (Exception e) {
                log.error("Token refresh failed for client={}: {}", client.getId(), e.getMessage());
            }
        }
    }
}
