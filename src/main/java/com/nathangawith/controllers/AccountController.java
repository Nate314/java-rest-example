package com.nathangawith.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

class LoginRequest {
	public String username;
	public String password;
}

@RestController
@RequestMapping("/auth")
public class AccountController {

    @RequestMapping(value = "/login/{username}/{password}",
    		method = RequestMethod.GET
    )
    public ResponseEntity<Object> getLogin(
    		@PathVariable String username,
    		@PathVariable String password
    	) throws Exception {

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
