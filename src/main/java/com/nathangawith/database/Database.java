package com.nathangawith.database;

import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;

public class Database {
	
	private static String connectionURL = "jdbc:mysql://localhost:3306/testing";
	private static String dbUser = "root";
	private static String dbPass = "root";

    public Database() {}

	public static <T extends Object> T testSelect(Class<T> type) {
		T test = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(connectionURL, dbUser, dbPass);
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
//            System.out.println();
//            for (Field f : type.getFields()) {
//            	String str = String.format("%s: %s", f.getName(), f.get(test));
//            	System.out.println(str);
//            }
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return test;
    }
}
