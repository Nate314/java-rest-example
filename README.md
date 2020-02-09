# Java REST API Example

This repository includes a basic REST API built with Spring framework and includes mysql database integration and auth0-JWT support.

I used these resources heavily to create this demo Java REST api:
- https://github.com/cagataygurturk/java-rest-example
- https://springframework.guru/spring-requestmapping-annotation/
- https://www.tutorialspoint.com/spring_boot/spring_boot_interceptor.htm



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


### Account Controller
No authentication required
```
/auth/login/{username}/{password}

returns a JWT that can be used for MathController requests
```

### Math Controller
requires JWT authentication
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
