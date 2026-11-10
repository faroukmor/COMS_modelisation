-- ============================================================================
-- Charity Fund Management System - Sample Data (Oracle)
-- ============================================================================
-- Includes:
--   - Default administrator account
--   - Sample donors, beneficiaries, donations, aid requests, distributions
-- Password for admin: Admin123  (hashed with PBKDF2WithHmacSHA256, 655360 iterations)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Default administrator account
-- Username: admin  |  Password: Admin123
-- ----------------------------------------------------------------------------
INSERT INTO users (username, password_hash, role, full_name, email, phone)
VALUES (
    'admin',
    'pbkdf2:sha256:655360$xvzkdecQvB987pxlv7RmdQ==$3w1dddvYrvRl38uXCXUbE6A2G1nv/iOClGZwwSwdTc0=',
    'ADMIN',
    'System Administrator',
    'admin@charity.org',
    '+213 00 00 00 00'
);

-- ----------------------------------------------------------------------------
-- Sample donors
-- ----------------------------------------------------------------------------
INSERT INTO donors (full_name, email, phone, address, total_donated)
VALUES ('Ahmed Benali', 'ahmed.benali@email.com', '+213 550 123 456', 'Algiers, Algeria', 50000.00);

INSERT INTO donors (full_name, email, phone, address, total_donated)
VALUES ('Fatima Zohra', 'fatima.z@email.com', '+213 550 789 012', 'Oran, Algeria', 30000.00);

INSERT INTO donors (full_name, email, phone, address, total_donated)
VALUES ('Karim Mansouri', 'karim.m@email.com', '+213 560 345 678', 'Constantine, Algeria', 20000.00);

-- ----------------------------------------------------------------------------
-- Sample beneficiaries (individuals)
-- ----------------------------------------------------------------------------
INSERT INTO beneficiaries (type, full_name, national_id, email, phone, address)
VALUES ('INDIVIDUAL', 'Sara Djamil', 'A123456789', 'sara.d@email.com', '+213 555 111 222', 'Algiers, Algeria');

INSERT INTO beneficiaries (type, full_name, national_id, email, phone, address)
VALUES ('INDIVIDUAL', 'Omar Khelif', 'B987654321', 'omar.k@email.com', '+213 555 333 444', 'Oran, Algeria');

-- ----------------------------------------------------------------------------
-- Sample beneficiaries (charity associations)
-- ----------------------------------------------------------------------------
INSERT INTO beneficiaries (type, organization_name, registration_number, representative_name, email, phone, address)
VALUES ('ASSOCIATION', 'Algerian Red Crescent', 'ARC-2023-001', 'Nadia Bouzid', 'contact@arc.dz', '+213 555 555 555', 'Algiers, Algeria');

INSERT INTO beneficiaries (type, organization_name, registration_number, representative_name, email, phone, address)
VALUES ('ASSOCIATION', 'Amal Child Welfare', 'ACW-2022-015', 'Samir Lahmar', 'info@amal.dz', '+213 555 777 888', 'Oran, Algeria');


-- ----------------------------------------------------------------------------
-- Sample donations (confirmed -> contribute to balance)
-- ----------------------------------------------------------------------------
INSERT INTO donations (donor_id, amount, donation_date, payment_method, status, description, reference)
VALUES (1, 50000.00, TIMESTAMP '2025-11-01 10:00:00', 'BANK_TRANSFER', 'CONFIRMED', 'Monthly donation', 'REF-2025-001');

INSERT INTO donations (donor_id, amount, donation_date, payment_method, status, description, reference)
VALUES (2, 30000.00, TIMESTAMP '2025-11-05 14:30:00', 'CASH', 'CONFIRMED', 'Zakat contribution', 'REF-2025-002');

INSERT INTO donations (donor_id, amount, donation_date, payment_method, status, description, reference)
VALUES (3, 20000.00, TIMESTAMP '2025-11-10 09:15:00', 'BANK_TRANSFER', 'CONFIRMED', 'Sadaqah', 'REF-2025-003');

-- A pending donation (does NOT contribute to balance)
INSERT INTO donations (donor_id, amount, donation_date, payment_method, status, description, reference)
VALUES (1, 10000.00, TIMESTAMP '2025-11-12 11:00:00', 'CASH', 'PENDING', 'Pending verification', 'REF-2025-004');

-- ----------------------------------------------------------------------------
-- Sample aid requests
-- ----------------------------------------------------------------------------
INSERT INTO aid_requests (beneficiary_id, beneficiary_type, requested_amount, approved_amount,
                          distributed_amount, description, submission_date, status, admin_notes)
VALUES (1, 'INDIVIDUAL', 15000.00, 15000.00, 15000.00,
        'Medical treatment for child', TIMESTAMP '2025-11-08 08:00:00', 'FULFILLED',
        'Fully distributed after approval');

INSERT INTO aid_requests (beneficiary_id, beneficiary_type, requested_amount, approved_amount,
                          distributed_amount, description, submission_date, status, reviewed_at, reviewed_by, admin_notes)
VALUES (2, 'INDIVIDUAL', 25000.00, 20000.00, 10000.00,
        'Family emergency assistance', TIMESTAMP '2025-11-09 09:00:00', 'APPROVED',
        TIMESTAMP '2025-11-10 10:00:00', 1, 'Partially distributed, remaining pending');

INSERT INTO aid_requests (beneficiary_id, beneficiary_type, requested_amount, approved_amount,
                          distributed_amount, description, submission_date, status)
VALUES (3, 'ASSOCIATION', 100000.00, 0.00, 0.00,
        'Support for orphanage winter supplies', TIMESTAMP '2025-11-11 10:00:00', 'PENDING');

INSERT INTO aid_requests (beneficiary_id, beneficiary_type, requested_amount, approved_amount,
                          distributed_amount, description, submission_date, status, reviewed_at, reviewed_by, admin_notes)
VALUES (4, 'ASSOCIATION', 50000.00, 0.00, 0.00,
        'Food parcels for displaced families', TIMESTAMP '2025-11-12 11:00:00', 'REJECTED',
        TIMESTAMP '2025-11-13 09:00:00', 1, 'Insufficient funds at time of review');

-- ----------------------------------------------------------------------------
-- Sample distributions (against approved requests)
-- ----------------------------------------------------------------------------
INSERT INTO distributions (aid_request_id, beneficiary_id, amount, distribution_date, payment_method, reference, description)
VALUES (1, 1, 15000.00, TIMESTAMP '2025-11-09 10:00:00', 'BANK_TRANSFER', 'DIST-2025-001', 'Full medical aid');

INSERT INTO distributions (aid_request_id, beneficiary_id, amount, distribution_date, payment_method, reference, description)
VALUES (2, 2, 10000.00, TIMESTAMP '2025-11-11 11:00:00', 'CASH', 'DIST-2025-002', 'First partial installment');

-- ============================================================================
-- End of sample data
-- ============================================================================

COMMIT;
