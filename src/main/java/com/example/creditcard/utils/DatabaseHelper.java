package com.example.creditcard.utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseHelper {
	public static Connection connectToDB(String dbName, String user, String pass, String url) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url+dbName, user, pass);
		} catch(Exception ex) {
			System.out.println("Exception while creating connection with database:"+ ex.getMessage());
		}
		return conn;
	}
}
