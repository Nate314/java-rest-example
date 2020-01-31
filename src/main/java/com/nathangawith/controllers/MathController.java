package com.nathangawith.controllers;

import com.nathangawith.database.Database;
import com.nathangawith.services.IMathService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

class MathRequest {
    public int a;
    public int b;
}

class MathResult {
    public int value;
    public MathResult(int val) {
        this.value = val;
    }
}

class TestDto {
	public String col_date;
	public int col_int;
	public String col_string;
}

@Component
@Path("/math")
public class MathController {

    @Autowired
    @Qualifier("math_service")
    private IMathService mathService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("add/{a}/{b}")
    public Response getAddResult(@PathParam("a") int a, @PathParam("b") int b) throws Exception {
    	TestDto t = Database.testSelect(TestDto.class);
    	System.out.println("Hey");
    	System.out.println(t.col_string);
        return Response.status(200).entity(
            new MathResult(mathService.getAddition(a, b))
        ).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("add")
    public Response postAddResult(MathRequest request) throws Exception {
        System.out.println("----------------");
        System.out.println(mathService);
        System.out.println(mathService.getAddition(request.a, request.b));
        System.out.println("----------------");
        int asdf = mathService.getAddition(request.a, request.b);
        return Response.status(200).entity(
            new MathResult(asdf)
        ).build();
    }
}

