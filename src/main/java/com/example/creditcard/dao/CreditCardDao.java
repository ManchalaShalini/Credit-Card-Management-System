package com.example.creditcard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.azure.security.keyvault.secrets.SecretClient;
import com.example.creditcard.constants.AkvConstants;
import com.example.creditcard.constants.DatabaseConstants;
import com.example.creditcard.exceptions.CreditCardException;
import com.example.creditcard.exceptions.UserException;
import com.example.creditcard.utils.AkvSecretHelper;
import com.example.creditcard.utils.DatabaseHelper;

import jakarta.annotation.PostConstruct;

@Component
public class CreditCardDao {
	
	private static String dbUrl = DatabaseConstants.dbUrl;
	
	private static String dbUser;
    private static String dbPwd;

    @PostConstruct
    private void initSecrets() {
        SecretClient client = AkvSecretHelper.getSecretClient(AkvConstants.akvName);
        dbUser = "postgres";//client.getSecret(AkvConstants.databaseUserSecretName).getValue();
        dbPwd = "2389";//client.getSecret(AkvConstants.databasePasswordSecretName).getValue();
    }
    

			
	public static List<String> getAkvSecretsByUserId(int userId) throws UserException{
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new UserException("Connection is null, object could not be created");
		}
		PreparedStatement st;
		List<String> akvSecrets = new ArrayList<>();
		try {
			String query = "select akvs.akv_secret_name from Card c join AKVSecret akvs on c.akv_secret_id = akvs.akv_secret_id where c.userId = ?";
			st = conn.prepareStatement(query);
			st.setInt(1, userId);
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				akvSecrets.add(rs.getString("akv_secret_name"));
			}	
			return akvSecrets;
		} catch(Exception e) {
			throw new UserException("Exception while getting Akv Secrets", e);
		}				
	}
	
	public static void storeCardAndSecretMetedataDetails(int userId, String akvSecretName) throws UserException {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new UserException("Connection is null, object could not be created");
		}
		PreparedStatement akvSecretSt;
		PreparedStatement cardSt;
		ResultSet rs = null;
		int akvId = 0;
		int akvSecretId = 0;			
			
		try {
			String insertAkvSecretQuery = "insert into akvsecrets (akvId, akvSecretName, state) values (?, ?, ?) returning akv_secret_id";
			akvSecretSt = conn.prepareStatement(insertAkvSecretQuery);
			akvSecretSt.setInt(1, akvId);
			akvSecretSt.setString(2, akvSecretName);
			akvSecretSt.setString(3, DatabaseConstants.active);
			rs = akvSecretSt.executeQuery();
			rs.next();
			akvSecretId = rs.getInt("akv_secret_id");			
		} 
		catch(Exception e) {
			throw new UserException("Exception occured while creating Akv Secret record", e);
		}
		
		try {
			String insertCardQuery = "insert into cards (userId, akvSecretId, state) values (?, ?, ?)";
			cardSt = conn.prepareStatement(insertCardQuery);
			cardSt.setInt(1, userId);
			cardSt.setInt(2, akvSecretId);
			akvSecretSt.setString(3, DatabaseConstants.active);
			cardSt.executeQuery();
		} 
		catch(Exception e) {
			throw new UserException("Exception occured while creating Card record", e);
		}		
	}

	public static void updateCardAndSecretMetadataToInactive(int userId) throws CreditCardException {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new CreditCardException("Connection is null, object could not be created");
		}
		PreparedStatement st;
		int akvSecretId = 0;
		try {
			String query = "update cards set state = ? where user_id = ?";
			st = conn.prepareStatement(query);
			st.setString(1, DatabaseConstants.inactive);
			st.setInt(2, userId);
			ResultSet rs = st.executeQuery();
			if(rs.next()) {
				akvSecretId = rs.getInt("akv_secret_id");
			}
		} 
		catch(Exception e) {
			throw new CreditCardException("Exception occured while updating card record as inactive", e);
		}
		
		try {
			String query = "update akvsecrets set state = ? where akv_secret_id = ?";
			st = conn.prepareStatement(query);
			st.setString(1, DatabaseConstants.inactive);
			st.setInt(2, akvSecretId);
			st.executeQuery();
		} 
		catch(Exception e) {
			throw new CreditCardException("Exception occured while updating card record as inactive", e);
		}				
	}	
}
