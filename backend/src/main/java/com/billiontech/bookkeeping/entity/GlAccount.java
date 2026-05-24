package com.billiontech.bookkeeping.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(name = "gl_accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"client_id", "qbo_account_id"})
})
public class GlAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "qbo_account_id", nullable = false, length = 100)
    private String qboAccountId;

    @Column(nullable = false)
    private String name;

    @Column(name = "account_type", length = 100)
    private String accountType;

    @Column(name = "account_sub_type", length = 100)
    private String accountSubType;

    @Column
    private Boolean active = true;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public String getQboAccountId() { return qboAccountId; }
    public void setQboAccountId(String qboAccountId) { this.qboAccountId = qboAccountId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getAccountSubType() { return accountSubType; }
    public void setAccountSubType(String accountSubType) { this.accountSubType = accountSubType; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
