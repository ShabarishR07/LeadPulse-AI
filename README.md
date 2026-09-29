# LeadPulse AI

LeadPulse AI is a full-stack digital marketing application for managing leads, understanding customer engagement, and reviewing campaign performance. It combines a server-rendered web interface with REST APIs and a relational data layer.

> **Project status:** Local development and automated tests are available. Render and Neon deployment configuration is included, but a live hosted deployment has not yet been verified.

## Features

- Manage customer profiles, activities, and campaigns.
- Calculate lead scores and status categories from engagement signals.
- View explainable lead factors, activity momentum, and suggested next actions.
- Review campaign measures such as leads, conversions, cost per lead, and attributed return.
- Analyze funnel and website tracking activity.
- Receive organization-associated tracking events and identify visitors when permitted.
- Accept signed lead webhooks with timestamp validation and idempotency protection.
- Rotate or revoke organization API keys.
- Use individual dashboard accounts with administrator-approved registration.
- Export or erase customer-related data through privacy endpoints.
- Export reports in spreadsheet and PDF formats.

## Technology

- **Backend:** Java 21, Spring Boot, Spring MVC, Spring Data JPA, Spring Security
- **Frontend:** Thymeleaf, HTML, CSS, JavaScript
- **Databases:** H2 for local development; PostgreSQL configured for production; MySQL driver retained
- **Build and tests:** Maven Wrapper, JUnit, Spring Boot Test
- **Deployment configuration:** Docker and Render Blueprint

## Run locally

### Requirements

- Java 21 or later
- Windows Command Prompt or PowerShell (the Maven Wrapper is included)

The default local profile uses an in-memory H2 database. No external database is needed for a basic local run, and data is cleared when the application stops.

### Windows Command Prompt

```cmd
cd /d C:\Marketing\marketingleadscoring
call mvnw.cmd spring-boot:run
```

### PowerShell

```powershell
cd C:\Marketing\marketingleadscoring
.\mvnw.cmd spring-boot:run
```

When Spring Boot has started, open [http://localhost:8080](http://localhost:8080). Stop the application with `Ctrl+C` in the terminal.

## Run tests

From the project directory:

```cmd
call mvnw.cmd test
```

The test suite covers application startup, service behavior, lead scoring, privacy operations, organization keys, tracking analytics, webhook security, and production security configuration.

## Main pages

- `/` — application landing page
- `/dashboard` — dashboard and analytics
- `/customers-page` — customer management
- `/activities` — activity management
- `/campaigns` — campaign management
- `/reports` — report exports
- `/integrations` — integration setup information
- `/login` — dashboard sign-in
- `/register` — request a dashboard account (administrator approval required)
- `/admin/accounts` — administrator review of pending account requests

Administrative pages and API access depend on the active Spring profile and security configuration.

## API overview

| Area | Example route |
|---|---|
| Dashboard | `GET /api/dashboard/stats` |
| Customers | `/api/customers` |
| Activities | `/api/activities` |
| Campaigns | `/api/campaigns` |
| Lead and tracking analytics | `/api/analytics/*` |
| Tracking events and visitor identification | `POST /api/tracking/events`, `POST /api/tracking/identify` |
| Signed lead webhooks | `POST /api/integrations/lead-events` |
| Customer privacy export and erase | `/api/privacy/customers/{id}/export`, `/api/privacy/customers/{id}` |

Integration endpoints require the appropriate organization credentials and, for lead webhooks, the required signature and idempotency headers. Refer to the integration page and controller implementations for request details.

## Deployment

The project includes a Render Blueprint and a PostgreSQL production schema for a Render Web Service with Neon PostgreSQL. Production uses schema validation; create the database tables before starting the service.

Follow [DEPLOYMENT.md](DEPLOYMENT.md) for the deployment sequence and environment-variable requirements. Keep all database passwords, bootstrap keys, API keys, and admin credentials in provider secret settings—never commit them to the repository.

## Security and demo limitations

- Do not expose organization API keys in public browser JavaScript. The current browser tracker is for local development; use server-to-server integration for testing.
- The first production administrator is seeded from Render environment variables. Public registrations remain pending until that administrator approves them; this is not yet a full organization-specific role-management system.
- Rate limiting is in-memory and is not shared across multiple application instances.
- Webhook signing currently uses the organization API key; a separate signing secret is a hardening item.
- Free hosting and database plans may sleep, impose quotas, or lack production backup and availability guarantees. Do not use the demo configuration for important customer data.
- Consent, privacy notices, retention, and applicable legal requirements must be addressed before collecting real visitor data.

This project is a learning and demonstration system, not a production security certification.

## Repository

[ShabarishR07/LeadPulse-AI](https://github.com/ShabarishR07/LeadPulse-AI)
