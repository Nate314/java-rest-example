package com.nathangawith.database;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Connection settings come from DB_URL, DB_USER and DB_PASSWORD. */
@Component
public class Database {

	private final String url;
	private final String user;
	private final String password;

	public Database(
			@Value("${app.db.url}") String url,
			@Value("${app.db.user:}") String user,
			@Value("${app.db.password:}") String password) {
		if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
			throw new IllegalStateException("DB_USER and DB_PASSWORD environment variables are required.");
		}
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public Connection getConnection() throws Exception {
		return DriverManager.getConnection(this.url, this.user, this.password);
	}
}
