CREATE TABLE IF NOT EXISTS organizations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    api_key_active BIT NULL,
    api_key_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL,
    name VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS dashboard_users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL,
    account_role VARCHAR(20) NOT NULL,
    approved BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    company VARCHAR(255),
    email VARCHAR(255),
    email_clicks INT,
    form_submissions INT,
    lead_score INT,
    lead_status VARCHAR(255),
    name VARCHAR(255),
    phone VARCHAR(255),
    social_media_clicks INT,
    website_visits INT,
    organization_id BIGINT,
    CONSTRAINT fk_customers_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE IF NOT EXISTS campaigns (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    attributed_revenue DOUBLE,
    budget DOUBLE,
    campaign_name VARCHAR(255),
    conversions INT,
    end_date DATE,
    leads_generated INT,
    platform VARCHAR(255),
    start_date DATE,
    status VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS activities (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    activity_date DATE,
    activity_type VARCHAR(255),
    customer_name VARCHAR(255),
    remarks VARCHAR(255),
    customer_id BIGINT,
    CONSTRAINT fk_activities_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE IF NOT EXISTS tracking_events (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(40) NOT NULL,
    occurred_at DATETIME(6) NOT NULL,
    page_url VARCHAR(500),
    referrer VARCHAR(500),
    session_id VARCHAR(100),
    visitor_id VARCHAR(100) NOT NULL,
    customer_id BIGINT,
    organization_id BIGINT NOT NULL,
    INDEX idx_tracking_org_visitor_time (organization_id, visitor_id, occurred_at),
    INDEX idx_tracking_org_customer (organization_id, customer_id),
    CONSTRAINT fk_tracking_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_tracking_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE IF NOT EXISTS webhook_receipts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL,
    received_at DATETIME(6) NOT NULL,
    organization_id BIGINT NOT NULL,
    CONSTRAINT uq_webhook_receipt_org_key UNIQUE (organization_id, idempotency_key),
    CONSTRAINT fk_webhook_receipt_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id)
);
