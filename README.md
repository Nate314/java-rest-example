# Java REST API Example

This repository includes a basic REST API built with Spring framework and includes mysql database integration and auth0-JWT support.

I used these resources heavily to create this demo Java REST api:
- https://github.com/cagataygurturk/java-rest-example
- https://springframework.guru/spring-requestmapping-annotation/
- https://www.tutorialspoint.com/spring_boot/spring_boot_interceptor.htm



## Run with Docker

No local JDK/Maven/MySQL install required. From the repo root:

```
docker compose up --build
```

This builds the app in a `maven:3.6-jdk-8` stage, runs it on `eclipse-temurin:8-jre-alpine`, and starts a `mysql:5.7` service seeded from `db/init.sql`. Wait for both containers to report healthy/started, then the API is available at `http://localhost:8080`, e.g.:

```
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "pw123"}'
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "pw123"}'
curl http://localhost:8080/math/add/3/4
```

Stop it with `docker compose down` (add `-v` to also drop the MySQL data volume).

## Browsing the database (phpMyAdmin)

The compose stack also starts a `phpmyadmin/phpmyadmin` container wired to the `mysql` service. With the stack running, open `http://localhost:8082`, log in with server `mysql`, username `root`, password `root`, and browse the `testing` database — including `users` (registered accounts, passwords stored as bcrypt hashes) and `math_operations` (an audit log of every addition, tied to the authenticated user who ran it).

## API docs (Swagger)

With the app running, interactive API docs are available at `http://localhost:8080/swagger-ui.html`, with the raw OpenAPI (Swagger 2.0) document at `http://localhost:8080/v2/api-docs`. Both endpoints are excluded from JWT auth so they're reachable without a token.

## End-to-end tests (Playwright)

The `e2e/` folder has [Playwright](https://playwright.dev) tests that exercise the running app over HTTP (login, the math endpoints, auth rejection) and render `swagger-ui.html` in a real browser to confirm the docs page actually works. They run against whatever `BASE_URL` you point them at, so start the app first (via Docker or `mvn spring-boot:run`), then:

```
cd e2e
npm install
npx playwright install chromium
BASE_URL=http://localhost:8080 npx playwright test
```

(On Windows PowerShell: `$env:BASE_URL="http://localhost:8080"; npx playwright test`.) `BASE_URL` defaults to `http://localhost:8080` if unset.

## Run and Test (without Docker)

To run the application type

```
mvn spring-boot:run
```

To execute unit and acceptance tests
```
mvn test
```

For unit tests and acceptance tests JUnit and REST Assured frameworks are used.

## Endpoints

**Important:** `Content-Type: application/json` header must be present to use API.

The most common HTTP status codes are returned when there is an error.


### Account Controller
No authentication required
```
/auth/register [POST] {"username": "...", "password": "..."}

creates a new user (password stored as a bcrypt hash). 409 Conflict if the
username is already taken, 400 Bad Request if either field is missing.

/auth/login [POST] {"username": "...", "password": "..."}

validates the username/password against the users table and, if correct,
returns a JWT that can be used for MathController requests. 401 Unauthorized
on an unknown username or wrong password.
```

### Math Controller
requires JWT authentication. The token's `preferred_username` claim must
belong to a user that still exists in the `users` table (checked on every
request, not just at login) — the interceptor rejects the request with a
500 if the user has since been removed. Every successful addition is logged
to `math_operations` with the authenticated username, both operands, and the
result.
```
/math/add [POST] {"a": a, "b": b}

returns a JSON response with the addition of a and b
{
    "value": a + b
}

/math/add/{a}/{b} [GET]

returns a JSON response with the addition of a and b
{
    "value": a + b
}
```
