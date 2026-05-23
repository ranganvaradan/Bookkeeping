-- V1: Initial schema for BillionTech Bookkeeping Platform

-- Multi-tenant CPA firms
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    subdomain VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Platform users scoped to a tenant
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN','REVIEWER','BOOKKEEPER')),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Bookkeeping clients (each connected to a QBO company)
CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    entity_type VARCHAR(20) CHECK (entity_type IN ('SOLE_PROP','LLC','S_CORP','C_CORP')),
    qbo_realm_id VARCHAR(100),
    qbo_access_token TEXT,
    qbo_refresh_token TEXT,
    token_expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Chart of accounts synced from QBO
CREATE TABLE gl_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES clients(id),
    qbo_account_id VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    account_type VARCHAR(100),
    account_sub_type VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    UNIQUE(client_id, qbo_account_id)
);

-- Ledger transactions (bills, invoices, payments, etc.)
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES clients(id),
    qbo_txn_id VARCHAR(100),
    txn_date DATE NOT NULL,
    type VARCHAR(30) CHECK (type IN ('BILL','INVOICE','PAYMENT','BANK_TXN','JOURNAL')),
    amount NUMERIC(15,2) NOT NULL,
    vendor_customer VARCHAR(255),
    description TEXT,
    gl_account_code VARCHAR(100),
    gl_account_name VARCHAR(255),
    tax_code VARCHAR(50),
    coding_confidence NUMERIC(4,3),
    coding_status VARCHAR(20) CHECK (coding_status IN ('AUTO_CODED','EXCEPTION','APPROVED','OVERRIDDEN')),
    raw_payload JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(client_id, qbo_txn_id)
);

-- Bank statement rows imported from CSV/OFX
CREATE TABLE bank_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES clients(id),
    statement_date DATE,
    txn_date DATE NOT NULL,
    description VARCHAR(500),
    amount NUMERIC(15,2) NOT NULL,
    balance NUMERIC(15,2),
    raw_row JSONB,
    import_hash VARCHAR(64) UNIQUE
);

-- Bank-to-ledger reconciliation pairs
CREATE TABLE reconciliation_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES clients(id),
    bank_txn_id UUID REFERENCES bank_transactions(id),
    ledger_txn_id UUID REFERENCES transactions(id),
    match_status VARCHAR(20) CHECK (match_status IN ('MATCHED','UNMATCHED','SUGGESTED')),
    period_date DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Coding exceptions flagged by AI or rules engine
CREATE TABLE exceptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID REFERENCES transactions(id),
    client_id UUID NOT NULL REFERENCES clients(id),
    exception_type VARCHAR(100) NOT NULL,
    description TEXT,
    suggested_action TEXT,
    severity VARCHAR(10) CHECK (severity IN ('LOW','MEDIUM','HIGH')),
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN','RESOLVED','ESCALATED')),
    assigned_to UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    resolved_at TIMESTAMPTZ
);

-- Audit trail for exception resolution actions
CREATE TABLE exception_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exception_id UUID NOT NULL REFERENCES exceptions(id),
    user_id UUID NOT NULL REFERENCES users(id),
    action VARCHAR(20) NOT NULL,
    old_gl_code VARCHAR(100),
    new_gl_code VARCHAR(100),
    old_gl_name VARCHAR(255),
    new_gl_name VARCHAR(255),
    timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- Background jobs run by AI agents
CREATE TABLE agent_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES clients(id),
    job_type VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','RUNNING','DONE','FAILED')),
    retry_count INT DEFAULT 0,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    result_summary JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Performance indexes for frequent query patterns
CREATE INDEX idx_transactions_client_id ON transactions(client_id);
CREATE INDEX idx_transactions_txn_date ON transactions(txn_date);
CREATE INDEX idx_transactions_coding_status ON transactions(coding_status);
CREATE INDEX idx_exceptions_client_status ON exceptions(client_id, status);
CREATE INDEX idx_agent_jobs_client_status ON agent_jobs(client_id, status);
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_clients_tenant_id ON clients(tenant_id);
