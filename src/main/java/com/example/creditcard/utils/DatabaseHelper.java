package com.example.creditcard.utils;

import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to help establish connections to the database.
 */
public class DatabaseHelper {
	
    private static final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);
	/**
     * Establishes and returns a JDBC connection to the specified database.
     *
     * @param dbName the name of the database to connect to.
     * @param user   the username for database authentication.
     * @param pass   the password for database authentication.
     * @param url    the JDBC URL prefix (e.g., "jdbc:postgresql://localhost:5432/").
     * @return a {@link Connection} object if connection is successful; null otherwise.
     */
	public static Connection connectToDB(String dbName, String user, String pass, String url) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url+dbName, user, pass);
		} catch(Exception ex) {
            logger.error("Exception while creating connection with database", ex);
        }
		return conn;
	}
}
