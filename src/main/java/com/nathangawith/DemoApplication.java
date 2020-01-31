package com.nathangawith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.nathangawith.controllers.AccountController;
import com.nathangawith.controllers.MathController;
import com.nathangawith.controllers.errorhandling.*;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class DemoApplication extends ResourceConfig {
    public DemoApplication() {
        // Controllers
        register(MathController.class);
        register(AccountController.class);

        // Error handling
        register(ServerErrorMapper.class);
        register(NotFoundMapper.class);
        register(BadRequestMapper.class);
        register(JsonMappingErrorHandler.class);
        register(UnprocessableEntityMapper.class);
    }

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
