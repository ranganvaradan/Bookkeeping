package com.billiontech.bookkeeping.controller.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,
        String entityType,
        UUID tenantId,
        String qboRealmId,
        OffsetDateTime lastSyncAt
) {}
