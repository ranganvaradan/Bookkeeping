package com.billiontech.bookkeeping.controller.dto;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        UUID tenantId,
        String role
) {}
