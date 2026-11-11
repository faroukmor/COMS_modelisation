# Charity Fund Management System

A desktop application (Java Swing) that lets a charitable organization record donations, manage beneficiaries, review aid requests and distribute funds, with the available balance always calculated from the recorded transactions.

> Built as an academic Software Engineering project. Data is stored in an Oracle database.

## Features

| Area | What it does |
|---|---|
| **Sign-in & sign-up** | Role-based login. Visitors can create a Donor, Beneficiary or Association account from the login screen. Passwords are hashed with PBKDF2 (never stored in clear text). |
| **Dashboard** (admin) | Confirmed donations, distributed funds, available balance, donor / beneficiary counts, pending requests, fund utilisation bar and recent activity. |
| **Donors** | Add, edit, delete and filter donors. |
| **Donations** | Record a donation (pending, or confirmed straight away), then confirm or cancel it. Only confirmed donations count toward the balance. |
| **Beneficiaries** | Individuals and charity associations, each with its own required fields. |
| **Aid requests** | Submit requests; an administrator approves (with an amount), rejects or marks them under review. |
| **Distributions** | Pay out approved aid. Each payout is one database transaction and is rolled back completely on any failure. |
| **Reports** | Financial summary and full history, live filter, export to CSV (opens correctly in Excel). |
| **Users** (admin) | List all accounts and add new users of any role, including other administrators. |
| **My Account** (donor / beneficiary / association) | A donor sees their donations and can donate; a beneficiary or association sees their requests and can submit new ones. |

### Roles

- **ADMIN**: all pages above.
- **DONOR**: *My Account* only (own donations, make a donation).
- **BENEFICIARY / ASSOCIATION**: *My Account* only (own aid requests, submit a request).

A donor / beneficiary account is linked to its record by **e-mail address**. Signing up creates both the account and the record; an e-mail that already exists in the system cannot be reused for a new account.

### Business rules enforced by the code

- Available balance = confirmed donations − distributed funds (never edited by hand).
- A donation must have a positive amount and an existing donor; a confirmed donation cannot be cancelled.
- A donor's `total donated` only grows when a donation is **confirmed**.
- Aid can only be distributed for an **approved** request, never more than the approved amount still remaining, and never more than the available balance. The request becomes *Fulfilled* once fully paid.
- Password policy: at least 8 characters, with at least one letter and one digit. Usernames are 3–30 characters (letters, digits, `.`, `_`, `-`) and case-insensitive.

## Technology

- Java **24** (JDK), Maven **3.9+**
- Swing with [FlatLaf](https://www.formdev.com/flatlaf/) 3.6.2 and custom-painted components
- Oracle Database **11g XE or later** via the `ojdbc8` driver (plain JDBC, no ORM)
- JUnit 5 and H2 (in Oracle compatibility mode) for the tests

## Getting started

The application needs an Oracle database, so cloning the repository is not enough: the database and a small configuration file must be set up once.

### What you need installed

1. **JDK 24** (`java -version`)
2. **Maven** (`mvn -version`)
3. **Oracle Database** (11g XE or later) with the SQL*Plus tool

### 1. Get the code

```
git clone <repository-url>
cd COMS_modelisation
```

### 2. Create the database user (once)

Open a terminal and connect as the database administrator:

```
sqlplus system
```

Then, inside `SQL>` (change the password if you like):

```sql
CREATE USER charity_app IDENTIFIED BY "Charity_123" DEFAULT TABLESPACE users QUOTA UNLIMITED ON users;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER TO charity_app;
EXIT;
```

(The same statements are in `src/main/resources/database/create_user.sql`.)

### 3. Create the tables and sample data (once)

Go to the scripts folder and connect as the new user:

```
cd src/main/resources/database
sqlplus charity_app
```

```sql
@schema.sql
@sample_data.sql
```

Expected: `Sequence created`, `Table created`, `Index created`, `Trigger created`, then `1 row created` repeatedly and `Commit complete`, with **no** `ERROR` lines. Quick check (should print `1 3 4 4 4 2`):

```sql
SELECT (SELECT COUNT(*) FROM users) u, (SELECT COUNT(*) FROM donors) d,
       (SELECT COUNT(*) FROM beneficiaries) b, (SELECT COUNT(*) FROM donations) dn,
       (SELECT COUNT(*) FROM aid_requests) r, (SELECT COUNT(*) FROM distributions) ds FROM dual;
```

To start over from scratch, run `@drop_all.sql` first (errors about missing objects are harmless).

### 4. Tell the application how to connect

Copy the example file and edit it. The real `db.properties` is in `.gitignore`, so your password is never committed.

```
cd ../../../..            # back to the project root
copy src\main\resources\db.properties.example src\main\resources\db.properties      (Windows)
cp   src/main/resources/db.properties.example src/main/resources/db.properties      (Linux / macOS)
```

```properties
db.url=jdbc:oracle:thin:@localhost:1521:XE
db.username=charity_app
db.password=Charity_123
```

`db.url` formats: `jdbc:oracle:thin:@HOST:PORT:SID` (Oracle 11g XE: SID is `XE`) or `jdbc:oracle:thin:@//HOST:PORT/SERVICE_NAME` (newer versions, e.g. `XEPDB1` / `FREEPDB1`). Any value can also be overridden from the command line, e.g. `-Ddb.password=...`.

### 5. Run

```
mvn clean compile exec:java
```

Sign in with the default administrator below. Whenever you change `db.properties`, rebuild (`mvn clean compile`) so the new file is copied to the classpath.

### Default login

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin123` | Administrator |

Create your own administrator from **Users → Add user**, and use a strong password for any real deployment. The sample donors, beneficiaries, donations and requests exist only to try the application; you can delete them.

## Running the tests

```
mvn test
```

No Oracle server is needed. The suite (100+ tests) has three layers:

1. **Service unit tests** with in-memory fakes: business rules, validation, sign-up, authentication.
2. **Integration tests** that run the real `schema.sql`, `sample_data.sql` and the real JDBC DAOs on an in-memory H2 database in Oracle mode: constraints, ids from sequences, transactions and rollback, the full money flow.
3. **UI tests** that drive the real screens without opening a window: buttons are clicked and the result is checked in the database.

H2 does not enforce every Oracle rule, so a few Oracle-specific rules are checked directly by tests (for example: identifiers of at most 30 characters, no `IDENTITY` columns, `DEFAULT` before `NOT NULL`).

## Troubleshooting

| Message | Cause and fix |
|---|---|
| `db.properties was not found` | Create `src/main/resources/db.properties` (step 4) and rebuild. |
| `ORA-12514` / `Listener refused the connection` | Wrong service name or SID in `db.url`. For 11g XE use `...@localhost:1521:XE`. Check that the Oracle service is running. |
| `ORA-01017 invalid username/password` | `db.username` / `db.password` do not match the user you created in step 2. |
| `ORA-00942 table or view does not exist` | The scripts were not run as `charity_app`, or `schema.sql` failed part-way. Run `drop_all.sql`, then `schema.sql` and `sample_data.sql` again, and read the output for `ERROR` lines. |
| `ORA-28000 account is locked` | `ALTER USER charity_app ACCOUNT UNLOCK;` as `system`. |
| Authentication / protocol error with correct credentials | Set `<ojdbc.version>` in `pom.xml` to `19.3.0.0` and rebuild (better match for 11g). |

## Project structure

```
src/main/java/com/charity/
  model/        entities and enums (User, Donor, Donation, AidRequest, Distribution, Role, ...)
  dao/          DAO interfaces and Oracle JDBC implementations
  service/      business rules (AuthenticationService, UserService, DonationService, DistributionService, ...)
  controller/   AppController: wires the layers together and handles navigation / sign-in
  view/         Swing screens, theme and reusable components
  database/     connection and configuration (db.properties / -D overrides)
  util/         password hashing and input validation
src/main/resources/
  db.properties.example     connection settings template
  database/                 create_user.sql, schema.sql, sample_data.sql, drop_all.sql
src/test/java/com/charity/
  service/  integration/  view/    the three test layers described above
```

Layers only depend downward: `view → controller → service → dao → database`. Money movements go through `DistributionService`, which opens one transaction for the distribution row and the request update.

## Known limitations

- **No e-mail verification.** The link between an account and its donor / beneficiary record is the e-mail address, which is why an already-registered e-mail is refused at sign-up.
- No password reset or change-password screen yet; an administrator can create a new account.
- One currency (DZD) and one general fund; no per-campaign funds.
- The balance check is not locked against two administrators disbursing at the same instant.
- Each database call opens a new connection (no connection pool), which is fine for a small desktop deployment.
- Tested automatically on H2 (Oracle mode) and by hand on Oracle 11g XE; other Oracle versions should work but are not part of the automated suite.

## Possible improvements

- Change-password and "forgot password" flows
- Connection pooling and row locking for disbursements
- PDF reports and date-range filters
- Multiple funds / campaigns
