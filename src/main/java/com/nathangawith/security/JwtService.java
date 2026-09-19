package com.nathangawith.security;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

/**
 * Single place that knows the JWT signing secret and token lifetime.
 */
@Component
public class JwtService {

	public static final long TOKEN_LIFETIME_SECONDS = 15 * 60;
	private static final int MIN_SECRET_LENGTH = 32;
	private static final String ISSUER = "java-rest-example";
	private static final String USERNAME_CLAIM = "preferred_username";

	private final Algorithm algorithm;
	private final JWTVerifier verifier;

	public JwtService(@Value("${app.jwt.secret:}") String secret) {
		if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
			throw new IllegalStateException(
					"JWT_SECRET environment variable is required and must be at least "
					+ MIN_SECRET_LENGTH + " characters long.");
		}
		this.algorithm = Algorithm.HMAC256(secret);
		this.verifier = JWT.require(this.algorithm).withIssuer(ISSUER).build();
	}

	public String createToken(String username) {
		Instant now = Instant.now();
		return JWT.create()
				.withIssuer(ISSUER)
				.withClaim(USERNAME_CLAIM, username)
				.withIssuedAt(Date.from(now))
				.withExpiresAt(Date.from(now.plusSeconds(TOKEN_LIFETIME_SECONDS)))
				.sign(this.algorithm);
	}

	/**
	 * @return the username claim of a validly signed, unexpired token
	 * @throws JWTVerificationException if the token is invalid in any way
	 */
	public String verifyAndGetUsername(String token) throws JWTVerificationException {
		String username = this.verifier.verify(token).getClaim(USERNAME_CLAIM).asString();
		if (username == null) {
			throw new JWTVerificationException("missing username claim");
		}
		return username;
	}
}
