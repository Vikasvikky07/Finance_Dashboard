# Finance Dashboard Backend

Backend assessment project for a finance dashboard system with JWT authentication, multi-role user access control, financial record management, and dashboard analytics.

This project uses **PostgreSQL** for persistence and **Flyway** for automatic schema migration. Flyway will create and update the required tables automatically when the application starts.

The application is fully deployed on Render, making the APIs publicly accessible without any local setup. This allows easy testing of endpoints and API documentation through live Swagger and OpenAPI links.
## API Docs

- [Swagger UI](https://finance-dashboard-bwwk.onrender.com/swagger-ui/index.html)
- [OpenAPI JSON](https://finance-dashboard-bwwk.onrender.com/v3/api-docs)

## API Base URL [use JWT to open]:
- https://finance-dashboard-bwwk.onrender.com/ [JWT token required]
- https://finance-dashboard-bwwk.onrender.com/actuator/health [health check and JWT token not required]

## What It Covers
- Deployed on Render with publicly accessible APIs for live testing.
- Login rate limiting
- PostgreSQL persistence with Flyway migrations
- Swagger/OpenAPI docs and a Postman collection
- User management with create, read, update, delete, password update, active/inactive status, and multi-role assignment
- JWT authentication with username and password
- Financial record CRUD with filtering by category, type, date range, text search, and pagination
- Dashboard summary API with total income, total expenses, net balance, category totals, recent activity, and monthly trends
- Validation and structured JSON error handling
- Soft delete for users and financial records

## Stack

- Java 17
- Spring Boot 4
- Spring Security
- Spring Data JPA
- Flyway
- PostgreSQL
- H2 for tests
- Static OpenAPI spec with hosted Swagger UI
- Render

## Access Model

- `VIEWER`: dashboard summary access
- `ANALYST`: dashboard summary plus financial record read access
- `ADMIN`: full user and record management

Users can hold more than one role at the same time through the `user_roles` join table.


Environment variables [local setup]:

- `DB_URL` default: `jdbc:postgresql://localhost:5432/finance_dashboard_v2`
- `DB_USERNAME` default: `postgres`
- `DB_PASSWORD` default: `9908`


Create the database:

```sql
CREATE DATABASE finance_dashboard_v2;
```

Default connection values [local setup]:

- `DB_URL=jdbc:postgresql://localhost:5432/finance_dashboard_v2`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=9908`

Run the app:

```bash
./gradlew bootRun
```

If you want to override the database on another machine:

```bash
DB_URL=jdbc:postgresql://localhost:5432/finance_dashboard_v2 DB_USERNAME=postgres DB_PASSWORD=your_password ./gradlew bootRun
```

Run tests:

```bash
./gradlew test
```

## Authentication

Authenticate first, then use the returned bearer token for protected endpoints.

All seeded users use password `password`.

- `admin`
- `analyst`
- `viewer`
- `inactive`

`inactive` is disabled and cannot log in.

Login:

```bash
curl -X POST https://finance-dashboard-bwwk.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"password\"}"
```

Use the returned token:

```bash
curl -H "Authorization: Bearer <token>" https://finance-dashboard-bwwk.onrender.com/api/dashboard/summary
```

## Example Endpoint Flow

After authentication, you can hit the main endpoints.

Get dashboard summary:

```bash
curl -H "Authorization: Bearer <token>" https://finance-dashboard-bwwk.onrender.com/api/dashboard/summary
```

Get paginated and searchable records:

```bash
curl -H "Authorization: Bearer <token>" "https://finance-dashboard-bwwk.onrender.com/api/records?search=software&type=EXPENSE&page=0&size=10"
```

Create a record:

```bash
curl -X POST https://finance-dashboard-bwwk.onrender.com/api/records \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d "{\"amount\":95.25,\"type\":\"EXPENSE\",\"category\":\"Internet\",\"date\":\"2026-04-04\",\"notes\":\"Monthly broadband bill\"}"
```

Create a user:

```bash
curl -X POST https://finance-dashboard-bwwk.onrender.com/api/users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Finance Manager\",\"username\":\"manager\",\"email\":\"manager@finance.local\",\"password\":\"strongPass123\",\"roles\":[\"ANALYST\",\"VIEWER\"],\"status\":\"ACTIVE\"}"
```

## Check Database

After hitting the endpoints, you can verify persisted data directly in PostgreSQL.

Example queries:

```sql
SELECT * FROM app_users;
SELECT * FROM roles;
SELECT * FROM user_roles;
SELECT * FROM financial_records;
```

## Main APIs

Auth:

- `POST /api/auth/login` with rate limiting on repeated failures

Dashboard:

- `GET /api/dashboard/summary`

Records:

- `GET /api/records`
- `GET /api/records/{id}`
- `POST /api/records`
- `PUT /api/records/{id}`
- `DELETE /api/records/{id}` soft deletes the record

Record filters:

- `category`
- `search`
- `type`
- `startDate`
- `endDate`
- `page`
- `size`

Users:

- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `PATCH /api/users/{id}/password`
- `DELETE /api/users/{id}` soft deletes the user

## Example User Payload

```json
{
  "name": "Finance Manager",
  "username": "manager",
  "email": "manager@finance.local",
  "password": "strongPass123",
  "roles": ["ANALYST", "VIEWER"],
  "status": "ACTIVE"
}
```

## Documentation Assets

- Swagger UI is available at `/swagger-ui/index.html`
- OpenAPI JSON is available at `/v3/api-docs`

## Notes

- For local development, use a fresh PostgreSQL database to avoid old schema drift from earlier project iterations.
- Flyway manages schema changes; tests run against H2 with the same migrations.
- Dashboard summaries are calculated in the service layer because the assignment scope is small and clarity matters more than premature optimization.
- Deleted users and records are retained in the database but hidden from normal reads.

## Assumptions and Tradeoffs

- JWT authentication is used instead of session auth to keep the API stateless and straightforward to evaluate.
- Role checks are enforced at the controller layer with Spring Security annotations because that keeps access rules explicit and easy to trace.
- Dashboard analytics are computed in the service layer from persisted records rather than pre-aggregated tables, which is acceptable for this assignment-scale dataset.
- Soft delete is used for users and records so data is not lost during normal management flows.
- Login rate limiting is intentionally lightweight and in-memory; it is suitable for a single-instance assessment project but not for a distributed production deployment.
- A fresh PostgreSQL database is recommended for local runs because the project evolved through multiple schema revisions during implementation.
