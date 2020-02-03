package com.nathangawith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.nathangawith.interceptors.JWTInterceptor;

@Component
public class Config extends WebMvcConfigurerAdapter {
	
	@Autowired
	JWTInterceptor jwtMiddleware;
	
	
	public Config() {
		super();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(jwtMiddleware);
	}
}
