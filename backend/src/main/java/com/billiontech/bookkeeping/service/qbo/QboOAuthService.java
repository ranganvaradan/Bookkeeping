package com.billiontech.bookkeeping.service.qbo;

import com.billiontech.bookkeeping.config.QboConfig;
import com.billiontech.bookkeeping.entity.Client;
import com.billiontech.bookkeeping.repository.ClientRepository;
import com.billiontech.bookkeeping.util.AesEncryptionUtil;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.config.Scope;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.InvalidRequestException;
import com.intuit.oauth2.exception.OAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class QboOAuthService {

    private static final Logger log = LoggerFactory.getLogger(QboOAuthService.class);

    private final QboConfig qboConfig;
    private final ClientRepository clientRepository;
    private final OAuth2PlatformClient oauthClient;
    private final OAuth2Config oauthConfig;

    public QboOAuthService(QboConfig qboConfig, ClientRepository clientRepository) {
        this.qboConfig = qboConfig;
        this.clientRepository = clientRepository;

        Environment env = "production".equalsIgnoreCase(qboConfig.getEnvironment())
                ? Environment.PRODUCTION : Environment.SANDBOX;

        this.oauthConfig = new OAuth2Config.OAuth2ConfigBuilder(
                qboConfig.getClientId(), qboConfig.getClientSecret())
                .callDiscoveryAPI(env)
                .buildConfig();

        this.oauthClient = new OAuth2PlatformClient(oauthConfig);
    }

    /**
     * Builds the QuickBooks authorization URL. The clientId is encoded in the
     * state parameter so the callback can associate tokens with the correct client.
     */
    public String generateAuthUrl(UUID clientId) {
        try {
            String csrf = clientId.toString();
            List<Scope> scopes = List.of(Scope.Accounting);
            return oauthConfig.prepareUrl(scopes, qboConfig.getRedirectUri(), csrf);
        } catch (InvalidRequestException e) {
            throw new QboException("Failed to generate QBO authorization URL", e);
        }
    }

    /**
     * Exchanges the authorization code for tokens and stores them encrypted.
     * Called from the OAuth callback endpoint.
     */
    @Transactional
    public UUID handleCallback(String authCode, String realmId, String state) {
        UUID clientId;
        try {
            clientId = UUID.fromString(state);
        } catch (IllegalArgumentException e) {
            throw new QboException("Invalid state parameter in QBO callback");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new QboException("Client not found: " + clientId));

        try {
            BearerTokenResponse tokenResponse =
                    oauthClient.retrieveBearerTokens(authCode, qboConfig.getRedirectUri());

            String encKey = qboConfig.getEncryptionKey();
            client.setQboRealmId(realmId);
            client.setQboAccessToken(AesEncryptionUtil.encrypt(tokenResponse.getAccessToken(), encKey));
            client.setQboRefreshToken(AesEncryptionUtil.encrypt(tokenResponse.getRefreshToken(), encKey));
            client.setTokenExpiresAt(
                    OffsetDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));

            clientRepository.save(client);
            log.info("QBO tokens stored for client={}, realmId={}", clientId, realmId);

            return clientId;
        } catch (OAuthException e) {
            throw new QboException("Failed to exchange QBO authorization code", e);
        }
    }

    public OAuth2PlatformClient getOauthClient() {
        return oauthClient;
    }

    public static class QboException extends RuntimeException {
        public QboException(String message) { super(message); }
        public QboException(String message, Throwable cause) { super(message, cause); }
    }
}
