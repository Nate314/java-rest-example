package com.nathangawith.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nathangawith.database.Database;
import com.nathangawith.services.IMathService;
import com.nathangawith.services.IOperationService;

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

@RestController
@RequestMapping("/math")
public class MathController {

    @Autowired
    @Qualifier("math_service")
    private IMathService mathService;

    @Autowired
    @Qualifier("operation_service")
    private IOperationService operationService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    private String currentUsername() {
    	return (String) httpServletRequest.getAttribute("username");
    }

    @RequestMapping(
    		value = "/add/{a}/{b}",
    		method = RequestMethod.GET
    )
    public ResponseEntity<MathResult> getAddResult(
    		@PathVariable("a") String a,
    		@PathVariable("b") String b)
    throws Exception {
    	TestDto t = new Database().testSelect(TestDto.class);
    	System.out.println("Hey");
    	System.out.println(t.col_string);
    	int intA = Integer.parseInt(a);
    	int intB = Integer.parseInt(b);
    	int result = mathService.getAddition(intA, intB);
    	operationService.logAddition(currentUsername(), intA, intB, result);
    	return new ResponseEntity<MathResult>(
    			new MathResult(result),
    			HttpStatus.OK);
    }

    @RequestMapping(
    		value = "/add",
    		method = RequestMethod.POST,
    		consumes = {"application/JSON"}
    )
    public ResponseEntity<MathResult> postAddResult(
    		@RequestBody MathRequest request)
    throws Exception {
        int result = mathService.getAddition(request.a, request.b);
        operationService.logAddition(currentUsername(), request.a, request.b, result);
    	return new ResponseEntity<MathResult>(
    			new MathResult(result),
    			HttpStatus.OK);
    }
}
