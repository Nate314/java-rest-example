package com.nathangawith.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.nathangawith.services.IUserService;

class LoginRequest {
	public String username;
	public String password;
}

class RegisterRequest {
	public String username;
	public String password;
}

class ApiError {
	public String message;
	public ApiError(String message) {
		this.message = message;
	}
}

@RestController
@RequestMapping("/auth")
public class AccountController {

    @Autowired
    @Qualifier("user_service")
    private IUserService userService;

    @RequestMapping(
    		value = "/register",
    		method = RequestMethod.POST,
    		consumes = {"application/JSON"}
    )
    public ResponseEntity<Object> postRegister(
    		@RequestBody RegisterRequest request
    	) throws Exception {

    	if (request.username == null || request.username.isEmpty()
    			|| request.password == null || request.password.isEmpty()) {
    		return new ResponseEntity<>(new ApiError("username and password are required"), HttpStatus.BAD_REQUEST);
    	}

    	if (this.userService.existsByUsername(request.username)) {
    		return new ResponseEntity<>(new ApiError("Username already exists"), HttpStatus.CONFLICT);
    	}

    	this.userService.register(request.username, request.password);
    	return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(
    		value = "/login",
    		method = RequestMethod.POST,
    		consumes = {"application/JSON"}
    )
    public ResponseEntity<Object> postLogin(
    		@RequestBody LoginRequest request
    	) throws Exception {

    	String username = request.username;

    	if (username == null || request.password == null
    			|| !this.userService.validateCredentials(username, request.password)) {
    		return new ResponseEntity<>(new ApiError("Invalid username or password"), HttpStatus.UNAUTHORIZED);
    	}

    	try {
    		int issued_at = (int) (System.currentTimeMillis() / 1000);
    		int expire_at = issued_at + (1000 * 60 * 15);
    	    Algorithm algorithm = Algorithm.HMAC256("secret");
    	    String token = JWT.create()
    	        .withIssuer("auth0")
//    	        .withArrayClaim("array_claim", new String[] {"str1", "str2"})
    	        .withClaim("preferred_username", username)
    	        .withClaim("iat", issued_at)
    	        .withClaim("exp", expire_at)
    	        .sign(algorithm);
    	    return new ResponseEntity<>(token, HttpStatus.OK);
    	} catch (JWTCreationException exception){
    	    //Invalid Signing configuration / Couldn't convert Claims.
    		throw exception;
    	}
    }
}
