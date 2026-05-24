package com.billiontech.bookkeeping.security;

import java.util.UUID;

/**
 * Holds the current tenant ID for the duration of a request using ThreadLocal.
 * Set by JwtFilter after token validation; cleared in the filter's finally block.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
