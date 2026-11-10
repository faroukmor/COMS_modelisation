-- ============================================================================
-- Creates the application's database user. Run ONCE as SYSTEM (or another DBA).
-- Then connect as charity_app and run schema.sql followed by sample_data.sql.
-- Change the password, and use the same one in db.properties.
-- ============================================================================
CREATE USER charity_app IDENTIFIED BY "ChangeMe_123"
    DEFAULT TABLESPACE users QUOTA UNLIMITED ON users;

GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER TO charity_app;
