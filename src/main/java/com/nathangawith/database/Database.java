package com.nathangawith.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/** Connection settings come from DB_URL, DB_USER and DB_PASSWORD. */
@Component
public class Database {

	private static final Logger log = LoggerFactory.getLogger(Database.class);

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

	/** Maps the first row of testing_table onto the given type (null if unavailable). */
	public <T> T testSelect(Class<T> type) {
		try (Connection con = getConnection();
				PreparedStatement stmt = con.prepareStatement("select * from testing_table limit 1");
				ResultSet rs = stmt.executeQuery()) {
			if (!rs.next()) {
				return null;
			}
			ResultSetMetaData rsmd = rs.getMetaData();
			JSONObject obj = new JSONObject();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String columnName = rsmd.getColumnLabel(i);
				obj.put(columnName, rs.getObject(i));
			}
			return new Gson().fromJson(obj.toString(), type);
		} catch (Exception e) {
			log.warn("testSelect failed: {}", e.getClass().getName());
			return null;
		}
	}
}
