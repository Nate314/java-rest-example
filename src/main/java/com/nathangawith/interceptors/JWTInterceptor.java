package com.nathangawith.interceptors;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.nathangawith.security.JwtService;
import com.nathangawith.services.IUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Requires a valid "Authorization: Bearer <jwt>" header on every route not
 * excluded in {@link com.nathangawith.Config}. Failures return 401 with a
 * generic JSON body. Nothing from the request is logged.
 */
@Component
public class JWTInterceptor implements HandlerInterceptor {

	private static final String BEARER_PREFIX = "Bearer ";

	private final IUserService userService;
	private final JwtService jwtService;

	@Autowired
	public JWTInterceptor(
		@Qualifier("user_service") IUserService userService,
		JwtService jwtService
	) {
		this.userService = userService;
		this.jwtService = jwtService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
			return unauthorized(response);
		}
		String token = authHeader.substring(BEARER_PREFIX.length()).trim();
		if (token.isEmpty()) {
			return unauthorized(response);
		}

		String username;
		try {
			username = this.jwtService.verifyAndGetUsername(token);
		} catch (JWTVerificationException e) {
			return unauthorized(response);
		}

		// The token is only honoured while the user still exists.
		if (!this.userService.existsByUsername(username)) {
			return unauthorized(response);
		}
		request.setAttribute("username", username);
		return true;
	}

	private static boolean unauthorized(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setHeader("WWW-Authenticate", "Bearer");
		response.setContentType("application/json");
		response.getWriter().write("{\"message\":\"Unauthorized\"}");
		return false;
	}
}
