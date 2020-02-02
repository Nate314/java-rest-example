package com.nathangawith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.nathangawith.middleware.JWTMiddleware;

@Component
//@EnableWebMvc
//@Configuration
public class Config extends WebMvcConfigurerAdapter {// extends ResourceConfig { // implements WebMvcConfigurer {
	
	@Autowired
	JWTMiddleware jwtMiddleware;
	
	
	public Config() {
		super();
//        register(JWTMiddleware.class);
//        register(MathController.class);
        
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		System.out.println("Adding interceptor");
		registry.addInterceptor(jwtMiddleware); // .addPathPatterns("**");
	}
	
//    @Bean
//    public JWTMiddleware authenticationInterceptor() {
//        return new JWTMiddleware();
//    }
}
