# Charity Fund Management System

## Project Overview
Charity Fund Management System is a desktop application for managing a charitable organization's financial donations and distributions. Built as an academic Software Engineering project, it prioritizes clean architecture, maintainability, and usability.

## Problem Statement
The charitable organization needed a software system to replace manual processes for tracking donations, managing beneficiaries, and distributing financial aid. Previously, the organization relied on spreadsheets and paper records, which led to difficulty in tracking donation history, calculating available funds, managing aid requests, and generating reports.

## Project Objectives
1. Register and manage donors with donation history tracking
2. Record financial donations with proper status tracking
3. Manage individual beneficiaries and charity associations
4. Receive and review financial aid requests
5. Approve or reject requests based on available funds
6. Distribute approved financial aid with proper validation
7. Monitor the organization's available financial balance
8. Generate basic financial and operational reports

## Main Features
- **Authentication**: Role-based login (ADMIN, DONOR, BENEFICIARY, ASSOCIATION) with public sign-up for donors, beneficiaries and associations; administrators can add any user (including other administrators) from the Users page
- **Self-service portal**: donors see their donations and can donate; beneficiaries/associations see their aid requests and can submit new ones (account and profile are linked by e-mail)
- **Donor Management**: Register, view, search, update donors
- **Donation Recording**: Record donations (cash/bank transfer), confirm/cancel
- **Beneficiary Management**: Individuals and charity associations
- **Aid Requests**: Submit, review, approve/reject
- **Financial Distributions**: Transactional disbursement with validation
- **Dashboard**: Financial overview with cards and tables
- **Reports**: CSV export of financial data

## Six Primary Use Cases
1. Authenticate User
2. Manage Donors
3. Record Donations
4. Manage Beneficiaries
5. Manage Aid Requests
6. Disburse Financial Aid

## Technology Stack
- **Language**: Java (JDK 24)
- **Build**: Maven 3.9.11
- **GUI**: Java Swing + FlatLaf (modern charitable green theme)
- **Database**: Oracle Database (11g XE or later)
- **JDBC**: Standard JDBC connectivity
- **Testing**: JUnit 5

## Technology Stack Details
- **FlatLaf 3.6.2**: Modern LookAndFeel with charitable green palette
- **Oracle JDBC driver**: ojdbc8 from Maven Central
- **JUnit 5.10.2**: Unit testing framework
- **PBKDF2WithHmacSHA256**: Password hashing for security

## Actors and Roles
- **Administrator**: Full access to all features (manage donors, record donations, review requests, distribute funds, access dashboards)
- **Donor**: Can record donations and view donation history
- **Beneficiary**: Can submit aid requests and track status

## Six Primary Use Cases
1. Authenticate User
2. Manage Donors
3. Record Donations
4. Manage Beneficiaries
5. Manage Aid Requests
6. Disburse Financial Aid

The dashboard and basic reports are interface features, not additional primary Use Cases.

## Technology Stack
- **Programming Language**: Java (JDK 24)
- **Build Tool**: Apache Maven 3.9.11
- **GUI**: Java Swing with FlatLaf styling
- **Database**: Oracle Database (11g XE or later)
- **Database Connectivity**: JDBC
- **Testing**: JUnit 5

## Software Architecture
The application follows a layered architecture with clear separation of responsibilities:

### Model Layer
Contains domain entities: User, Donor, Beneficiary, Individual, CharityAssociation, Donation, AidRequest, Distribution.

### DAO Layer
Handles database operations using JDBC with prepared statements to prevent SQL injection.

### Service Layer
Contains business logic and validation for all financial rules and operations.

### Controller Layer
Connects the GUI with the service layer, handling user interactions.

### View Layer
Contains Swing screens, dialogs, forms, tables, and dashboard components.

### Database Layer
Handles database connections and configuration with Oracle SQL scripts.

## Project Directory Structure
```
COMS_modelisation/
├── src/main/java/com/charity/model/          # Domain entities (8 classes)
├── src/main/java/com/charity/dao/            # Data access objects (12 files)
├── src/main/java/com/charity/service/        # Business logic (7 services)
├── src/main/java/com/charity/controller/     # Application controller (2 classes)
├── src/main/java/com/charity/view/           # Swing UI (15+ windows)
├── src/main/java/com/charity/util/           # Utilities (Password hashing)
├── src/test/java/com/charity/service/        # JUnit 5 tests (15 classes)
├── src/main/resources/database/              # SQL schema and sample data
├── pom.xml                                   # Maven configuration
├── README.md                                 # Project overview
└── document.md                               # Detailed documentation
```

## Installation Instructions
1. Ensure Java JDK 24 is installed
2. Ensure Apache Maven 3.9.11 is installed
3. Install Oracle Database (11g XE or later)
4. As SYSTEM run `create_user.sql` (creates the `charity_app` user), then connect as `charity_app` and run `schema.sql` to create the tables, sequences and triggers (`drop_all.sql` removes them again)
5. Run `sample_data.sql` to insert the default admin account and sample records (the script ends with `COMMIT`)
6. Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties` and set your Oracle connection details (any value can also be overridden with `-Ddb.url=... -Ddb.username=... -Ddb.password=...`)
7. Compile with `mvn compile`
8. Run with `mvn compile exec:java`

## Default Login Credentials
- **Username**: admin
- **Password**: Admin123
- **Role**: ADMIN (full access to all features)

## Screenshots
*(Screenshots would show the GUI interface - placeholder for demo)*

## Testing Instructions
Run all tests: `mvn test`
Run specific test: `mvn test -Dtest=AidRequestServiceTest`
The suite (100+ tests) has three layers:
- service unit tests using in-memory fakes (business rules, validation, sign-up, authentication);
- integration tests that run the real `schema.sql`, `sample_data.sql` and the real JDBC DAOs on an in-memory H2 database in Oracle-compatibility mode (SQL scripts, constraints, transactions/rollback, end-to-end money flow);
- UI tests that drive the real screens without opening a window (buttons are clicked, results checked in the database).
No Oracle server is needed to run `mvn test`.

## Known Limitations
- Database: Oracle Database required (not H2/SQLite)
- Desktop only: No web or mobile support
- Single organization: Designed for one charitable organization
- No online payments: Does not implement real payment processing
- Password hashing: Tested in controlled environments

## Future Improvements
- PDF report generation with charts
- Email notifications for status changes
- Advanced search and filtering
- Enhanced audit logging
- Database backup/restore functionality

## Development Commits
The project follows meaningful commits:
1. chore: initialize Maven Java project
2. docs: add project overview and initial README
3. feat: implement domain models
4. feat: implement MySQL database schema and connection
5. feat: implement DAO layer
6. feat: implement authentication and role management
7. feat: implement donor management
8. feat: implement donation management
9. feat: implement beneficiary management
10. feat: implement aid request management
11. feat: implement financial distribution logic
12. feat: implement administrator dashboard
13. feat: implement reports
14. style: improve GUI and user experience
15. test: add unit and integration tests
16. docs: complete README and setup instructions

## Summary
The Charity Fund Management System is a complete, functional desktop application for managing a charitable organization's financial operations. It demonstrates proper software engineering practices with clean architecture, maintainable code, and comprehensive testing covering all six primary use cases.
