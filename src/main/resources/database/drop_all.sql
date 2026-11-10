-- Removes everything schema.sql creates (run as charity_app). Errors about missing objects can be ignored.
DROP TABLE distributions CASCADE CONSTRAINTS;
DROP TABLE aid_requests CASCADE CONSTRAINTS;
DROP TABLE donations CASCADE CONSTRAINTS;
DROP TABLE beneficiaries CASCADE CONSTRAINTS;
DROP TABLE donors CASCADE CONSTRAINTS;
DROP TABLE users CASCADE CONSTRAINTS;
DROP SEQUENCE seq_users;
DROP SEQUENCE seq_donors;
DROP SEQUENCE seq_beneficiaries;
DROP SEQUENCE seq_donations;
DROP SEQUENCE seq_aid_requests;
DROP SEQUENCE seq_distributions;
