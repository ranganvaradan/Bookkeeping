package com.billiontech.bookkeeping.controller.dto;

import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,
        String entityType,
        UUID tenantId
) {}
