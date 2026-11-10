-- ============================================================================
-- Charity Fund Management System - Oracle Database Schema
-- ============================================================================
-- Run this script as a user with CREATE TABLE privileges (e.g., charity_app).
-- Compatible with Oracle Database 11g XE and later (sequences + triggers, no IDENTITY columns).
-- Run it in SQL*Plus or SQL Developer ("Run Script"); the lines containing only "/" end PL/SQL blocks.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 0. sequences (primary keys; filled in by the triggers in section 8)
-- ----------------------------------------------------------------------------
CREATE SEQUENCE seq_users START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_donors START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_beneficiaries START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_donations START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_aid_requests START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_distributions START WITH 1 INCREMENT BY 1 NOCACHE;

-- ----------------------------------------------------------------------------
-- 1. users - system accounts for authentication & RBAC
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    id               NUMBER(19,0)    PRIMARY KEY,
    username         VARCHAR2(100)   NOT NULL UNIQUE,
    password_hash    VARCHAR2(255)   NOT NULL,
    role             VARCHAR2(20)    NOT NULL,
    full_name        VARCHAR2(200)   NOT NULL,
    email            VARCHAR2(200),
    phone            VARCHAR2(50),
    created_at       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_user_role CHECK (role IN ('ADMIN','DONOR','BENEFICIARY','ASSOCIATION'))
);

-- ----------------------------------------------------------------------------
-- 2. donors - people/entities providing financial donations
-- ----------------------------------------------------------------------------
CREATE TABLE donors (
    id               NUMBER(19,0)    PRIMARY KEY,
    full_name        VARCHAR2(200)   NOT NULL,
    email            VARCHAR2(200),
    phone            VARCHAR2(50),
    address          VARCHAR2(500),
    total_donated    NUMBER(15,2)    DEFAULT 0 NOT NULL,
    created_at       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_donor_total CHECK (total_donated >= 0)
);


-- ----------------------------------------------------------------------------
-- 3. beneficiaries - individuals & charity associations (single-table inheritance)
-- ----------------------------------------------------------------------------
CREATE TABLE beneficiaries (
    id                 NUMBER(19,0)    PRIMARY KEY,
    type               VARCHAR2(20)    NOT NULL,
    full_name          VARCHAR2(200),          -- individual only
    national_id        VARCHAR2(50),           -- individual only (unique)
    organization_name  VARCHAR2(200),          -- association only
    registration_number VARCHAR2(100),         -- association only (unique)
    representative_name VARCHAR2(200),         -- association only
    email              VARCHAR2(200),
    phone              VARCHAR2(50),
    address            VARCHAR2(500),
    created_at         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_beneficiary_type CHECK (type IN ('INDIVIDUAL','ASSOCIATION')),
    CONSTRAINT uq_national_id UNIQUE (national_id),
    CONSTRAINT uq_registration_number UNIQUE (registration_number)
);

-- ----------------------------------------------------------------------------
-- 4. donations - financial donations received from donors
-- ----------------------------------------------------------------------------
CREATE TABLE donations (
    id               NUMBER(19,0)    PRIMARY KEY,
    donor_id         NUMBER(19,0)    NOT NULL,
    amount           NUMBER(15,2)    NOT NULL,
    donation_date    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    payment_method   VARCHAR2(20)    NOT NULL,
    status           VARCHAR2(20)    NOT NULL,
    description      VARCHAR2(1000),
    reference        VARCHAR2(200),
    CONSTRAINT fk_donation_donor FOREIGN KEY (donor_id) REFERENCES donors(id),
    CONSTRAINT chk_donation_amount CHECK (amount > 0),
    CONSTRAINT chk_donation_payment CHECK (payment_method IN ('CASH','BANK_TRANSFER')),
    CONSTRAINT chk_donation_status CHECK (status IN ('PENDING','CONFIRMED','CANCELLED'))
);

-- ----------------------------------------------------------------------------
-- 5. aid_requests - financial aid requests submitted by beneficiaries
-- ----------------------------------------------------------------------------
CREATE TABLE aid_requests (
    id                NUMBER(19,0)    PRIMARY KEY,
    beneficiary_id    NUMBER(19,0)    NOT NULL,
    beneficiary_type  VARCHAR2(20)    NOT NULL,
    requested_amount  NUMBER(15,2)    NOT NULL,
    approved_amount   NUMBER(15,2)    DEFAULT 0 NOT NULL,
    distributed_amount NUMBER(15,2)   DEFAULT 0 NOT NULL,
    description       VARCHAR2(1000)  NOT NULL,
    submission_date   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status            VARCHAR2(20)    DEFAULT 'PENDING' NOT NULL,
    reviewed_at       TIMESTAMP,
    reviewed_by       NUMBER(19,0),
    admin_notes       VARCHAR2(2000),
    CONSTRAINT fk_request_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id),
    CONSTRAINT fk_request_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id),
    CONSTRAINT chk_request_amount CHECK (requested_amount > 0),
    CONSTRAINT chk_request_approved CHECK (approved_amount >= 0),
    CONSTRAINT chk_request_distributed CHECK (distributed_amount >= 0),
    CONSTRAINT chk_request_status CHECK (status IN ('PENDING','UNDER_REVIEW','APPROVED','REJECTED','FULFILLED')),
    CONSTRAINT chk_request_approved_le_req CHECK (approved_amount <= requested_amount)
);

-- ----------------------------------------------------------------------------
-- 6. distributions - financial distributions against approved aid requests
-- ----------------------------------------------------------------------------
CREATE TABLE distributions (
    id                NUMBER(19,0)    PRIMARY KEY,
    aid_request_id    NUMBER(19,0)    NOT NULL,
    beneficiary_id    NUMBER(19,0)    NOT NULL,
    amount            NUMBER(15,2)    NOT NULL,
    distribution_date TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    payment_method    VARCHAR2(20)    NOT NULL,
    reference         VARCHAR2(200),
    description       VARCHAR2(1000),
    CONSTRAINT fk_distribution_request FOREIGN KEY (aid_request_id) REFERENCES aid_requests(id),
    CONSTRAINT fk_distribution_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id),
    CONSTRAINT chk_distribution_amount CHECK (amount > 0),
    CONSTRAINT chk_distribution_payment CHECK (payment_method IN ('CASH','BANK_TRANSFER'))
);

-- ----------------------------------------------------------------------------
-- 7. indexes for common queries
-- ----------------------------------------------------------------------------
CREATE INDEX idx_donations_donor_id      ON donations(donor_id);
CREATE INDEX idx_donations_status        ON donations(status);
CREATE INDEX idx_donations_date          ON donations(donation_date);

CREATE INDEX idx_requests_beneficiary_id ON aid_requests(beneficiary_id);
CREATE INDEX idx_requests_status         ON aid_requests(status);
CREATE INDEX idx_requests_date           ON aid_requests(submission_date);

CREATE INDEX idx_distributions_request   ON distributions(aid_request_id);
CREATE INDEX idx_distributions_date      ON distributions(distribution_date);

-- ----------------------------------------------------------------------------
-- 8. triggers: assign the primary key from the sequence when none is supplied
--    (replaces IDENTITY columns, which Oracle only has from 12c)
-- ----------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_users_bi
BEFORE INSERT ON users
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := seq_users.NEXTVAL;
END;
/

CREATE OR REPLACE TRIGGER trg_donors_bi
BEFORE INSERT ON donors
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := seq_donors.NEXTVAL;
END;
/

CREATE OR REPLACE TRIGGER trg_beneficiaries_bi
BEFORE INSERT ON beneficiaries
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := seq_beneficiaries.NEXTVAL;
END;
/

CREATE OR REPLACE TRIGGER trg_donations_bi
BEFORE INSERT ON donations
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := seq_donations.NEXTVAL;
END;
/

CREATE OR REPLACE TRIGGER trg_aid_requests_bi
BEFORE INSERT ON aid_requests
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := seq_aid_requests.NEXTVAL;
END;
/

CREATE OR REPLACE TRIGGER trg_distributions_bi
BEFORE INSERT ON distributions
FOR EACH ROW
WHEN (NEW.id IS NULL)
BEGIN
    :NEW.id := seq_distributions.NEXTVAL;
END;
/

-- ----------------------------------------------------------------------------
-- End of schema
-- ============================================================================
