package com.billiontech.bookkeeping.service;

import com.billiontech.bookkeeping.entity.RefreshToken;
import com.billiontech.bookkeeping.entity.User;
import com.billiontech.bookkeeping.repository.RefreshTokenRepository;
import com.billiontech.bookkeeping.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public record LoginResult(String accessToken, String refreshToken,
                              UUID userId, UUID tenantId, String role) {}

    @Transactional
    public LoginResult login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!user.getActive()) {
            throw new AuthException("Account is disabled");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }

        UUID userId = user.getId();
        UUID tenantId = user.getTenant().getId();
        String role = user.getRole();

        String accessToken = jwtService.generateAccessToken(userId, tenantId, role);
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(OffsetDateTime.now().plus(REFRESH_TOKEN_TTL));
        refreshTokenRepository.save(refreshToken);

        return new LoginResult(accessToken, refreshTokenValue, userId, tenantId, role);
    }

    @Transactional
    public String refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        return jwtService.generateAccessToken(
                user.getId(), user.getTenant().getId(), user.getRole());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public static class AuthException extends RuntimeException {
        public AuthException(String message) {
            super(message);
        }
    }
}
