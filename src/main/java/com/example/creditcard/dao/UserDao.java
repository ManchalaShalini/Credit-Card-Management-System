package com.example.creditcard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.azure.security.keyvault.secrets.SecretClient;
import com.example.creditcard.constants.AkvConstants;
import com.example.creditcard.constants.DatabaseConstants;
import com.example.creditcard.exceptions.UserException;
import com.example.creditcard.model.User;
import com.example.creditcard.utils.AkvSecretHelper;
import com.example.creditcard.utils.DatabaseHelper;

import jakarta.annotation.PostConstruct;

/**
 * DAO class to perform operations on the `users` table like create, read, update, and delete.
 */
@Component
public class UserDao{
		
	private String dbUrl = DatabaseConstants.dbUrl;
	
	private static String dbUser;
    private static String dbPwd;
   
    /**
     * Initializes sensitive database credentials after bean construction using Azure Key Vault.
     * This method is automatically called after the Spring component is initialized.
     */
    @PostConstruct
    private static void initSecrets() {
        SecretClient client = AkvSecretHelper.getSecretClient(AkvConstants.akvName);
        dbUser = client.getSecret(AkvConstants.databaseUserSecretName).getValue();
        dbPwd = client.getSecret(AkvConstants.databasePasswordSecretName).getValue();
    }
    
    /**
     * Inserts a new user record into the database.
     * @param user The User object to be created.
     * @return The created User object with generated ID and timestamps.
     * @throws UserException If insertion fails or DB is unreachable.
     */
	public User createUser(User user) throws UserException {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if (conn == null)
		{
			throw new UserException("Connection is null, object could not be created");
		}
		
		PreparedStatement st;
		try {
			String query = "insert into users (user_name, email_address, state) values (?, ?, ?) RETURNING user_id, created_at, modified_at";
			st = conn.prepareStatement(query);
			st.setString(1, user.getUserName());
			st.setString(2, user.getEmailAddress());
			st.setString(3, DatabaseConstants.ACTIVE);
			ResultSet rs = st.executeQuery();
			if(rs.next()) {
				int id = rs.getInt("user_id");
				Timestamp created_at = rs.getTimestamp("created_at");
				Timestamp modified_at = rs.getTimestamp("modified_at");
	            user.setUserId(id);
	            user.setCreatedOn(created_at);
	            user.setModifiedOn(modified_at);
			}
		} 
		catch(Exception e) {
			throw new UserException("Exception occured while creating user record", e);
		}
		return user;
	}
	
	 /**
     * Retrieves user details by user ID.
     * @param userId The user ID.
     * @return The User object or null if not found.
     * @throws UserException If DB connection fails or query throws error.
     */
	public User getUser(int userId) throws UserException {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new UserException("Connection is null, object could not be created");
		}

		PreparedStatement st;
		try {
			String query = "select * from users where user_id = ? and state = ?";
			st = conn.prepareStatement(query);
			st.setInt(1, userId);
			st.setString(2, DatabaseConstants.ACTIVE);
			ResultSet rs = st.executeQuery();
						
			User user = new User();
			if(rs.next()) {
				user.setUserId(rs.getInt("user_id"));
				user.setUserName(rs.getString("user_name"));
				user.setEmailAddress(rs.getString("email_address"));
				user.setCreatedOn(rs.getTimestamp("created_at"));
				user.setModifiedOn(rs.getTimestamp("modified_at"));
				return user;
			}
		} 
		catch(Exception e) {
			throw new UserException("Exception occured while retrieving user details", e);
		}	
		return null;
	}
	
	 /**
     * Soft deletes a user by setting their state to INACTIVE.
     * @param userId The ID of the user to be deleted.
     * @return True if the record was updated successfully.
     * @throws UserException If update fails or user doesn't exist.
     */
	public boolean deleteUser(int userId) throws UserException {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new UserException("Connection is null, object could not be created");
		}
		PreparedStatement st;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
			String utcTime = ZonedDateTime.now(ZoneOffset.UTC).format(formatter);
			String query = "update users set state = ?, modified_at = ? where user_id = ?";
			st = conn.prepareStatement(query);
			st.setString(1, DatabaseConstants.INACTIVE);
			st.setTimestamp(2, Timestamp.valueOf(utcTime));
			st.setInt(3, userId);
			int rowsUpdated = st.executeUpdate();
			if(rowsUpdated == 0) {
				throw new UserException("User does not exist");
			}
			return rowsUpdated == 1;// TODO: what to do if rows inserted in not 1, throw exception here or in another layer - decide in end
		} 
		catch(Exception e) {
			throw new UserException("Exception occured while updating user record as inactive", e);
		}				
	}
	
	/**
     * Updates a user’s name and email by user ID.
     * @param userId The ID of the user to update.
     * @param name New name of the user.
     * @param email New email address of the user.
     * @return True if the update was successful.
     * @throws UserException If user doesn't exist or update fails.
     */
	public boolean updateUser(int userId, String name, String email) throws UserException {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new UserException("Connection is null, object could not be created");	
		}
		PreparedStatement st;
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
			String utcTime = ZonedDateTime.now(ZoneOffset.UTC).format(formatter);
			String query = "update users set user_name = ?, email_address = ?, modified_at = ? where user_id = ?";
			st = conn.prepareStatement(query);
			st.setString(1, name);
			st.setString(2, email);
			st.setTimestamp(3, Timestamp.valueOf(utcTime));
			st.setInt(4, userId);
			int rowsUpdated = st.executeUpdate();
			if(rowsUpdated == 0) {
				throw new UserException("User does not exist");
			}
			return rowsUpdated == 1;				
		} 
		catch(Exception e) {
			throw new UserException("Exception occured while updating user record", e);
		}
	}			
}