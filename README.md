# Java REST API Example

This repository includes a basic REST API built with Jersey and Spring framework and for demonstration purposes.

This is a modified clone of https://github.com/cagataygurturk/java-rest-example that includes mysql database integration and auth0-JWT support

## Run and Test

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

### Add a transaction

```
/math/add/{a}/{b} [GET]

returns a JSON response with the addition of a and b
{
    "value": a + b
}
```