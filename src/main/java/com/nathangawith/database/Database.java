package com.nathangawith.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

class DBConfig {
	public String connectionURL = "jdbc:mysql://localhost:3306/testing";
	public String dbUser = "root";
	public String dbPass = "root";
}

public class Database {

	private final DBConfig config;

    public Database() throws Exception {
    	List<String> jsonLines = Arrays.asList(FileIO.readFileLines("config.json"));
    	String json = String.join("\n", jsonLines);
    	this.config = new Gson().fromJson(json, DBConfig.class);
    }

	public <T extends Object> T testSelect(Class<T> type) {
		T test = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(this.config.connectionURL, this.config.dbUser, this.config.dbPass);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from testing_table");
            JSONArray json = new JSONArray();
            ResultSetMetaData rsmd = rs.getMetaData();
            while(rs.next()) {
                int numColumns = rsmd.getColumnCount();
                JSONObject obj = new JSONObject();
                for (int i=1; i<=numColumns; i++) {
                    String column_name = rsmd.getColumnName(i);
                    obj.put(column_name, rs.getObject(column_name));
                }
                json.put(obj);
            }
            System.out.println(json);
            System.out.println(json.get(0));
            test = new Gson().fromJson(json.get(0).toString(), type);
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return test;
    }
}
