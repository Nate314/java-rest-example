# Java REST API Example

This repository includes a basic REST API built with Spring Boot 3 (Java 17), MySQL 8.4 and auth0 java-jwt.

I used these resources heavily to create this demo Java REST api:
- https://github.com/cagataygurturk/java-rest-example
- https://springframework.guru/spring-requestmapping-annotation/
- https://www.tutorialspoint.com/spring_boot/spring_boot_interceptor.htm

## Run with Docker

No local JDK/Maven/MySQL install required. From the repo root:

```
docker compose up --build
```

This builds the app with Maven on JDK 17, runs it on `eclipse-temurin:17-jre-alpine` as a non-root user with a read-only filesystem, and starts a `mysql:8.4` service seeded from `db/`. The API is then available at `http://localhost:8080`, e.g.:

```
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "pw12345678"}'
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "pw12345678"}'
curl http://localhost:8080/math/add/3/4 -H "Authorization: Bearer <token from login>"
```

Stop it with `docker compose down` (add `-v` to also drop the MySQL data volume).

### Configuration and secrets

All configuration comes from environment variables. Nothing secret is committed or baked into the image. `docker-compose.yml` falls back to obvious local-only defaults so a fresh clone runs with one command. **Those defaults are for local demos only: copy `.env.example` to `.env` (git-ignored) and change every value before any non-local use.**

| Variable | Used by | Purpose |
| --- | --- | --- |
| `JWT_SECRET` | app | HMAC key for signing JWTs, at least 32 characters. Required: the app refuses to start without it. |
| `DB_URL` | app | JDBC URL (default `jdbc:mysql://localhost:3306/testing`, compose sets the `mysql` host). |
| `DB_USER`, `DB_PASSWORD` | app, mysql init | Credentials of the least-privilege app account. Required. No single quotes. |
| `MYSQL_ROOT_PASSWORD` | mysql, phpMyAdmin login | MySQL root password. The app never uses it. |
| `SWAGGER_ENABLED` | app | `true` (default) or `false` to disable Swagger UI and the OpenAPI document. |
| `APP_PORT`, `PMA_PORT`, `MYSQL_PORT` | compose | Host ports (8080, 8082, 3306). MySQL and phpMyAdmin bind to `127.0.0.1` only. |

The app account is created by `db/99-app-user.sh` on first initialisation of the data volume and only has `SELECT` on `testing_table` and `SELECT, INSERT` on `users` and `math_operations`: no `DROP`, `UPDATE`, `DELETE` or access to other schemas. Changing `DB_PASSWORD` later requires `docker compose down -v` (or changing the password inside MySQL), because the account is only created once.

Running without Docker: start MySQL 8 with the schema in `db/01-schema.sql`, create a user, then `JWT_SECRET=... DB_USER=... DB_PASSWORD=... mvn spring-boot:run`.

## Browsing the database (phpMyAdmin)

The compose stack also starts phpMyAdmin wired to the `mysql` service, bound to `http://localhost:8082` on the local machine only. There is no auto-login: log in with the app account (`DB_USER`/`DB_PASSWORD`, read only browsing of `users` and `math_operations`) or as `root` with `MYSQL_ROOT_PASSWORD` for administration. Registered passwords are stored as bcrypt hashes, and `math_operations` is an audit log of every addition tied to the authenticated user.

## API docs (Swagger)

With the app running, interactive API docs are at `http://localhost:8080/swagger-ui.html` and the raw OpenAPI 3 document at `http://localhost:8080/v3/api-docs`. Both are excluded from JWT auth. Set `SWAGGER_ENABLED=false` to turn both off (recommended outside local demos).

The math endpoints are marked as secured (padlock icon). To call them from Swagger UI:

1. In `account-controller`, `POST /auth/register`, then `POST /auth/login`, and copy the JWT from the response.
2. Click **Authorize**, paste the token only (Swagger UI adds the `Bearer ` prefix itself), click **Authorize**, then **Close**.
3. Expand `math-controller`, click **Try it out**, then **Execute**.

## End-to-end tests (Playwright)

The `e2e/` folder has [Playwright](https://playwright.dev) tests that exercise the running app over HTTP (register, login, token lifetime, auth rejection, input validation, throttling, security headers) and drive Swagger UI in a real browser, including the Authorize flow. Start the app first, then:

```
cd e2e
npm install
npx playwright install chromium
BASE_URL=http://localhost:8080 npx playwright test
```

(On Windows PowerShell: `$env:BASE_URL="http://localhost:8080"; npx playwright test`.) The tests forge tokens with `JWT_SECRET`, which defaults to the compose dev default; export it if you changed it. The login throttle test needs the app to see a stable client address, which is true for a normal local run.

## Endpoints

**Important:** `Content-Type: application/json` header must be present to use API.

Errors are JSON `{"message": "..."}` with no stack traces or internal detail.

### Account Controller
No authentication required
```
/auth/register [POST] {"username": "...", "password": "..."}

Creates a user (password stored as a bcrypt hash). Username: 3-64 characters of
letters, digits, . _ @ -. Password: at least 8 characters and at most 72 bytes
(bcrypt ignores anything longer). 400 on invalid input, 409 if the username is taken.

/auth/login [POST] {"username": "...", "password": "..."}

Returns a JWT valid for 15 minutes. 401 with the same body for an unknown user and
a wrong password (a dummy bcrypt check keeps timing similar). After 5 failed logins
for the same address and username within 15 minutes, 429 until the window passes.
```

### Math Controller
Requires `Authorization: Bearer <jwt>`. Missing, malformed, expired, forged or unsigned
tokens, and tokens for users that no longer exist, get 401. Every successful addition is
logged to `math_operations` with the authenticated username, both operands and the result.
```
/math/add [POST] {"a": a, "b": b}
/math/add/{a}/{b} [GET]

Both return {"value": a + b}. Non-numeric path operands return 400.
```

## Security notes

Summary of what is hardened and what is deliberately left as a follow-up.

- **Dependencies:** Spring Boot 3.5.x (Spring Framework 6, Tomcat 10.1, Jackson) with Boot managed versions bumped in `pom.xml` where a newer patched release exists, `mysql-connector-j`, current `org.json`, `gson` and `java-jwt` 4.x. Swagger moved from springfox (unmaintained, Boot 2.5 max) to springdoc-openapi.
- **JWT:** secret from `JWT_SECRET` (min 32 chars, fail fast), one `JwtService` owns signing and verification, HS256 only, issuer checked, 15 minute lifetime (`exp - iat = 900`).
- **Auth errors:** 401 JSON responses, no logging of headers, tokens or URLs. Public routes are matched by exact path, not URL substring.
- **Database:** app uses a least-privilege account, all SQL uses `PreparedStatement`, credentials come from the environment.
- **Containers:** non-root user, read-only root filesystem, all capabilities dropped, `no-new-privileges`, pinned major/minor base image tags, `.dockerignore` keeps secrets and config out of the image, MySQL and phpMyAdmin on `127.0.0.1` only.
- **Headers:** `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Cache-Control: no-store` on `/auth` and `/math`, no `Server` header.
- **Known limits / follow-ups:** the login throttle is in-memory and per instance (use a shared store or a gateway for multiple replicas), and it keys on the direct client address (behind a proxy, configure forwarded headers deliberately). Registration returns 409 for a taken username, which reveals whether a name exists. There is no TLS termination (put a reverse proxy in front), no refresh tokens or token revocation, and no CSP header (it would break Swagger UI's inline scripts).
