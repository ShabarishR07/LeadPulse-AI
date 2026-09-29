# Free demo deployment: Render + Neon PostgreSQL

LeadPulse AI is a Spring Boot application that serves Thymeleaf pages and REST APIs.
Deploy it to Render as a Docker **Web Service**, not as a Static Site. For a free demo,
use Neon PostgreSQL. This requires no-cost-tier caveats and a PostgreSQL schema migration;
it is not a durable production setup.

## Free-tier limitations to understand first

- Render's free web service spins down after inactivity, so the first request can be slow.
- Neon Free compute scales to zero, and its free plan has strict storage, compute, history,
  and egress limits. Review the current plan limits before using real traffic.
- Free databases may be unsuitable for backups, availability guarantees, or important data.
- Do not store real customer or visitor data in this demonstration setup.
- Upgrade both services and configure backups before any real business use.

## 1. Create a Neon PostgreSQL project

1. Create a Neon project and database.
2. In Neon Console, select **Connect** and copy the pooled connection host for your branch.
   The pooled host usually contains `-pooler`.
3. Do not expose the connection password or paste it into source code.
4. Convert the Neon connection details into the JDBC format:

   ```text
   jdbc:postgresql://POOLER_HOST/DB_NAME?sslmode=require
   ```

   Keep username and password in separate environment variables. Do not include them in
   `DB_URL`. Use Neon-provided host, database, role, and password values.
5. Run `src/main/resources/db/production-schema-postgresql.sql` against a new, empty Neon
   database using the Neon SQL Editor or a trusted PostgreSQL client.

The production profile uses `ddl-auto=validate`: it checks the existing schema and does not
automatically create or mutate production tables.

## 2. Deploy from GitHub to Render

1. Confirm the latest project changes, including the PostgreSQL driver and schema file, have
   been pushed to the private repository.
2. In Render, choose **New > Blueprint** and connect
   [ShabarishR07/LeadPulse-AI](https://github.com/ShabarishR07/LeadPulse-AI).
3. Render should detect `render.yaml` and configure a free Docker Web Service.
4. Set all secret environment variables in Render's environment settings. Never put secrets in
   GitHub or `render.yaml`.
5. Deploy and wait for the build and health check.
6. Open the assigned `onrender.com` URL and verify the homepage returns HTTP 200.

## 3. Render environment variables

| Variable | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `production` |
| `DB_URL` | Neon JDBC URL using the pooled host and `sslmode=require` |
| `DB_USERNAME` | Neon role/user |
| `DB_PASSWORD` | Neon database password |
| `DB_DRIVER` | `org.postgresql.Driver` |
| `DB_DIALECT` | `org.hibernate.dialect.PostgreSQLDialect` |
| `LEADPULSE_BOOTSTRAP_KEY` | Strong, randomly generated secret |
| `LEADPULSE_ADMIN_USERNAME` | Chosen admin username |
| `LEADPULSE_ADMIN_PASSWORD_BCRYPT` | BCrypt hash of a strong password |
| `LEADPULSE_ALLOWED_ORIGINS` | Exact HTTPS origins; never `*` |

Render supplies `PORT` automatically. The Render Blueprint sets the driver and dialect. Keep
`DB_URL`, username, password, and application secrets in Render's protected environment
settings. The admin password environment value must be a BCrypt hash, not the plaintext
password.

## 4. Dashboard accounts

- `LEADPULSE_ADMIN_USERNAME` and `LEADPULSE_ADMIN_PASSWORD_BCRYPT` seed the initial
  administrator account. The user enters the original password on the sign-in page, not its hash.
- The configured administrator username and BCrypt hash are synchronized at application startup.
  Updating the hash in Render is the supported way to reset that administrator's password.
- Users can request accounts at `/register`; new accounts cannot sign in until an administrator
  approves them at `/admin/accounts`.
- Dashboard pages use an in-app form login and session cookie. The production session expires
  after 30 minutes of inactivity, and users can explicitly sign out.
- Before deploying this account feature to an existing database, run the updated
  `src/main/resources/db/production-schema-postgresql.sql` in Neon. Its `CREATE TABLE IF NOT EXISTS`
  statements add the new account table without replacing existing tables or data.

## 5. Check deployment logs

If the service fails its startup or schema-validation check:

1. Confirm the Neon schema SQL was run against the correct database.
2. Confirm `DB_URL` starts with `jdbc:postgresql://` and includes `sslmode=require`.
3. Confirm the database username/password and driver/dialect environment variables.
4. Review Render logs and share only the error text after removing hostnames, usernames,
   passwords, API keys, and tokens.

## 6. Before using real data

- Use paid persistent compute and database tiers, and verify backup and restore procedures.
- The production UI currently has one platform admin, not separate company users.
- The in-memory rate limiter only works across one application instance.
- Do not place organization API keys in public browser JavaScript. The current browser tracker
  is local-development-only; use server-to-server webhook calls for testing.
- Webhook HMAC currently uses the organization API key as its signing secret.
- Establish visitor consent, privacy notices, retention, export, and deletion procedures.

This guide prepares a learning/demo deployment, not a production security certification.
