# Java REST API Example

This repository includes a basic REST API built with Jersey and Spring framework and for demonstration purposes.

This project started as a clone of https://github.com/cagataygurturk/java-rest-example
I have added mysql database integration and auth0-JWT support.

Other resources I used for the basics around creating a Java REST api:
https://springframework.guru/spring-requestmapping-annotation/
https://www.tutorialspoint.com/spring_boot/spring_boot_interceptor.htm

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
