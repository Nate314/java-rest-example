package com.nathangawith.controllers;

import java.sql.SQLIntegrityConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nathangawith.security.ApiError;
import com.nathangawith.security.CredentialRules;
import com.nathangawith.security.JwtService;
import com.nathangawith.security.LoginThrottle;
import com.nathangawith.services.IUserService;

import jakarta.servlet.http.HttpServletRequest;

class LoginRequest {
	public String username;
	public String password;
}

class RegisterRequest {
	public String username;
	public String password;
}

@RestController
@RequestMapping("/auth")
public class AccountController {

    private static final ResponseEntity<Object> INVALID_LOGIN =
    		new ResponseEntity<>(new ApiError("Invalid username or password"), HttpStatus.UNAUTHORIZED);

    @Autowired
    @Qualifier("user_service")
    private IUserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private LoginThrottle loginThrottle;

    @RequestMapping(
    		value = "/register",
    		method = RequestMethod.POST,
    		consumes = {"application/JSON"}
    )
    public ResponseEntity<Object> postRegister(
    		@RequestBody RegisterRequest request
    	) throws Exception {

    	if (request == null
    			|| !CredentialRules.isValidUsername(request.username)
    			|| !CredentialRules.isValidPassword(request.password)) {
    		return new ResponseEntity<>(new ApiError(
    				"username must be " + CredentialRules.USERNAME_MIN + "-" + CredentialRules.USERNAME_MAX
    				+ " characters (letters, digits, . _ @ -) and password must be at least "
    				+ CredentialRules.PASSWORD_MIN + " characters and at most "
    				+ CredentialRules.PASSWORD_MAX_BYTES + " bytes"), HttpStatus.BAD_REQUEST);
    	}

    	if (this.userService.existsByUsername(request.username)) {
    		return new ResponseEntity<>(new ApiError("Username already exists"), HttpStatus.CONFLICT);
    	}

    	try {
    		this.userService.register(request.username, request.password);
    	} catch (SQLIntegrityConstraintViolationException e) {
    		// Lost a race with a concurrent registration of the same name.
    		return new ResponseEntity<>(new ApiError("Username already exists"), HttpStatus.CONFLICT);
    	}
    	return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(
    		value = "/login",
    		method = RequestMethod.POST,
    		consumes = {"application/JSON"}
    )
    public ResponseEntity<Object> postLogin(
    		@RequestBody LoginRequest request,
    		HttpServletRequest httpRequest
    	) throws Exception {

    	if (request == null || request.username == null || request.password == null) {
    		return INVALID_LOGIN;
    	}
    	String address = httpRequest.getRemoteAddr();
    	String username = request.username;

    	if (this.loginThrottle.isBlocked(address, username)) {
    		return new ResponseEntity<>(new ApiError("Too many failed attempts, try again later"),
    				HttpStatus.TOO_MANY_REQUESTS);
    	}

    	// Inputs that could never have been registered are rejected without a DB lookup.
    	boolean plausible = CredentialRules.isValidUsername(username)
    			&& CredentialRules.isValidPassword(request.password);
    	if (!plausible || !this.userService.validateCredentials(username, request.password)) {
    		this.loginThrottle.recordFailure(address, username);
    		return INVALID_LOGIN;
    	}

    	this.loginThrottle.recordSuccess(address, username);
    	return new ResponseEntity<>(this.jwtService.createToken(username), HttpStatus.OK);
    }
}
