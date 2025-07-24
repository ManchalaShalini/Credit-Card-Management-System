package com.example.creditcard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.azure.security.keyvault.secrets.SecretClient;
import com.example.creditcard.constants.AkvConstants;
import com.example.creditcard.constants.DatabaseConstants;
import com.example.creditcard.exceptions.UserException;
import com.example.creditcard.model.User;
import com.example.creditcard.utils.AkvSecretHelper;
import com.example.creditcard.utils.DatabaseHelper;

import jakarta.annotation.PostConstruct;

@Component
public class UserDao{
		
	private String dbUrl = DatabaseConstants.dbUrl;
	
	private static String dbUser;
    private static String dbPwd;
    
    @PostConstruct
    private static void initSecrets() {
        SecretClient client = AkvSecretHelper.getSecretClient(AkvConstants.akvName);
        dbUser = "postgres"; //client.getSecret(AkvConstants.databaseUserSecretName).getValue();
        dbPwd = "2389"; //client.getSecret(AkvConstants.databasePasswordSecretName).getValue();
    }
    
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
			st.setString(3, DatabaseConstants.active);
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
	
	public User getUser(int userId) throws UserException {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new UserException("Connection is null, object could not be created");
		}
		System.out.println("Connection establised with database");
		PreparedStatement st;
		try {
			String query = "select * from users where user_id = ? and state = ?";
			st = conn.prepareStatement(query);
			st.setInt(1, userId);
			st.setString(2, DatabaseConstants.active);
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
	
	public boolean deleteUser(int userId) throws UserException {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new UserException("Connection is null, object could not be created");
		}
		PreparedStatement st;
		try {
			String query = "update users set state = ? where user_id = ?";
			st = conn.prepareStatement(query);
			st.setString(1, DatabaseConstants.inactive);
			st.setInt(2, userId);
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
	
	public boolean updateUser(int userId, String name, String email) throws UserException {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new UserException("Connection is null, object could not be created");	
		}
		PreparedStatement st;
		try {
			String query = "update users set user_name = ?, email_address = ? where user_id = ?";
			st = conn.prepareStatement(query);
			st.setString(1, name);
			st.setString(2, email);
			st.setInt(3, userId);
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