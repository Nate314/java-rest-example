package com.nathangawith.security;

/** JSON error body: a short message only, never internal detail. */
public class ApiError {
	public String message;

	public ApiError(String message) {
		this.message = message;
	}
}
