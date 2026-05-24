package com.billiontech.bookkeeping.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(name = "entity_type", length = 20)
    private String entityType;

    @Column(name = "qbo_realm_id", length = 100)
    private String qboRealmId;

    @Column(name = "qbo_access_token")
    private String qboAccessToken;

    @Column(name = "qbo_refresh_token")
    private String qboRefreshToken;

    @Column(name = "token_expires_at")
    private OffsetDateTime tokenExpiresAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getQboRealmId() { return qboRealmId; }
    public void setQboRealmId(String qboRealmId) { this.qboRealmId = qboRealmId; }

    public String getQboAccessToken() { return qboAccessToken; }
    public void setQboAccessToken(String qboAccessToken) { this.qboAccessToken = qboAccessToken; }

    public String getQboRefreshToken() { return qboRefreshToken; }
    public void setQboRefreshToken(String qboRefreshToken) { this.qboRefreshToken = qboRefreshToken; }

    public OffsetDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(OffsetDateTime tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
