package com.nathangawith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.nathangawith.interceptors.JWTInterceptor;

@Configuration
public class Config implements WebMvcConfigurer {

	@Autowired
	JWTInterceptor jwtMiddleware;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// Public routes are excluded by exact path pattern, not by substring
		// matching on the URL (which crafted paths could bypass).
		registry.addInterceptor(jwtMiddleware)
			.excludePathPatterns(
				"/auth/login",
				"/auth/register",
				"/error",
				"/swagger-ui.html",
				"/swagger-ui/**",
				"/v3/api-docs",
				"/v3/api-docs/**"
			);
	}
}
