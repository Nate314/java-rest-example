package com.nathangawith.controllers;

import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;;

class LoginRequest {
	public String username;
	public String password;
}

@Component
@Path("/auth")
public class AccountController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("login/{username}/{password}")
    public Response getLogin(
    		@PathParam("username") String username,
    		@PathParam("password") String password
    	) throws Exception {

    	try {
    	    Algorithm algorithm = Algorithm.HMAC256("secret");
    	    String token = JWT.create()
    	        .withIssuer("auth0")
//    	        .withArrayClaim("array_claim", new String[] {"str1", "str2"})
    	        .withClaim("preferred_username", username)
    	        .sign(algorithm);
            return Response.status(200).entity(token).build();
    	} catch (JWTCreationException exception){
    	    //Invalid Signing configuration / Couldn't convert Claims.
    		throw exception;
    	}
    }
}
