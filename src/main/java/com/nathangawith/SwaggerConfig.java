package com.nathangawith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	private static final String SECURITY_SCHEME_NAME = "Bearer";

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.nathangawith.controllers"))
				.paths(PathSelectors.any())
				.build()
				.securitySchemes(Collections.singletonList(apiKey()))
				.securityContexts(Collections.singletonList(securityContext()));
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("Java REST API Example")
				.description("Demo REST API with JWT auth and MySQL-backed math endpoints.")
				.version("1.0")
				.build();
	}

	private ApiKey apiKey() {
		// The JWTInterceptor reads the raw "Authorization" header and splits on
		// "Bearer ", so the Swagger "Authorize" dialog must be given the FULL
		// header value, e.g. "Bearer eyJhbGciOi...", not just the token.
		return new ApiKey(SECURITY_SCHEME_NAME, "Authorization", "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder()
				.securityReferences(defaultAuth())
				.forPaths(PathSelectors.regex("/math/.*"))
				.build();
	}

	private List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = { authorizationScope };
		return Arrays.asList(new SecurityReference(SECURITY_SCHEME_NAME, authorizationScopes));
	}
}
