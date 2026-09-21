package com.nathangawith.security;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/** Input constraints for usernames and passwords. */
public final class CredentialRules {

	public static final int USERNAME_MIN = 3;
	public static final int USERNAME_MAX = 64;
	public static final int PASSWORD_MIN = 8;
	/** bcrypt only uses the first 72 bytes of its input. */
	public static final int PASSWORD_MAX_BYTES = 72;

	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_.@-]+$");

	private CredentialRules() {
	}

	public static boolean isValidUsername(String username) {
		return username != null
				&& username.length() >= USERNAME_MIN
				&& username.length() <= USERNAME_MAX
				&& USERNAME_PATTERN.matcher(username).matches();
	}

	public static boolean isValidPassword(String password) {
		return password != null
				&& password.length() >= PASSWORD_MIN
				&& password.getBytes(StandardCharsets.UTF_8).length <= PASSWORD_MAX_BYTES
				&& !password.trim().isEmpty();
	}
}
