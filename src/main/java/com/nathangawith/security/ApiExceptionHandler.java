package com.nathangawith.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
			NumberFormatException.class })
	public ResponseEntity<ApiError> badRequest(Exception e) {
		return new ResponseEntity<>(new ApiError("Bad request"), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ApiError> unsupportedMediaType(Exception e) {
		return new ResponseEntity<>(new ApiError("Unsupported media type"), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiError> methodNotAllowed(Exception e) {
		return new ResponseEntity<>(new ApiError("Method not allowed"), HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> internalError(Exception e) {
		// Log the class only: driver messages can contain connection details.
		log.error("Unhandled exception of type {}", e.getClass().getName());
		return new ResponseEntity<>(new ApiError("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
