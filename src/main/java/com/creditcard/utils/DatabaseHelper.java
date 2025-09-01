package com.creditcard.utils;

import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class related to the database.
 */
@Component
public class DatabaseHelper {
	
    private Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);
    
	/**
     * Establishes and returns a JDBC connection to the specified database.
     * @param dbName the name of the database to connect to.
     * @param user   the username for database authentication.
     * @param password   the password for database authentication.
     * @param url    the JDBC URL prefix (e.g., "jdbc:postgresql://localhost:5432/").
     * @return a Connection object if connection is successful; null otherwise.
     */
	public Connection getConnection(String dbName, String user, String password, String url) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url+dbName, user, password);
		} catch(Exception ex) {
            logger.error("Exception while creating connection with database", ex);
        }
		return conn;
	}
}