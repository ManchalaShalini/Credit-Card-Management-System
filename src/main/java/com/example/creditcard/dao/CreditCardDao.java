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

/**
 * Data Access Object (DAO) class for managing operations related to credit card metadata
 * and integration with Azure Key Vault.
 *
 * This class includes logic to:
 * - Retrieve AKV secret names associated with a user.
 * - Store new card and secret metadata entries.
 * - Mark card and secret entries as inactive when delete apis are called.
 * - Securely initialize and use secrets from Azure Key Vault.
 */
@Component
public class CreditCardDao {
	
	private static String dbUrl = DatabaseConstants.dbUrl;
	
	private static String dbUser;
    private static String dbPwd;

    /**
     * Initializes sensitive database credentials after bean construction using Azure Key Vault.
     * This method is automatically called after the Spring component is initialized.
     */
    @PostConstruct
    private void initSecrets() {
        SecretClient client = AkvSecretHelper.getSecretClient(AkvConstants.akvName);
        dbUser = client.getSecret(AkvConstants.databaseUserSecretName).getValue();
        dbPwd = client.getSecret(AkvConstants.databasePasswordSecretName).getValue();
    }
    

    /**
     * Retrieves a list of AKV (Azure Key Vault) secret names linked to a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of AKV secret names associated with the user's stored card records.
     * @throws UserException if the database connection fails or an SQL error occurs.
     */	
	public static List<String> getActiveAkvSecretsByUserId(int userId) throws UserException{
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new UserException("Connection is null, object could not be created");
		}
		PreparedStatement st;
		List<String> akvSecrets = new ArrayList<>();
		try {
			String query = "select akvs.akv_secret_name from cards c join akvsecrets akvs on c.akv_secret_id = akvs.akv_secret_id where "
					+ "c.user_id = ? and akvs.state = ?";
			st = conn.prepareStatement(query);
			st.setInt(1, userId);
			st.setString(2, DatabaseConstants.ACTIVE);
			ResultSet rs = st.executeQuery();
			while(rs.next()) {
				akvSecrets.add(rs.getString("akv_secret_name"));
			}	
			return akvSecrets;
		} catch(Exception e) {
			throw new UserException("Exception while getting Akv Secrets", e);
		}				
	}
	
	/**
	 * Stores metadata in the database for a new card and its corresponding Azure Key Vault secret.
	 *
	 * - Inserts a new AKV secret entry and retrieves its ID.
	 * - Inserts a new card entry referencing the created AKV secret ID.
	 *
	 * @param userId         The user ID for whom the card is being stored.
	 * @param akvSecretName  The name of the AKV secret used to store the card securely.
	 * @throws UserException if any error occurs during database operations.
	 */
	public static void storeCardAndSecretMetedataDetails(int userId, String akvSecretName) throws UserException {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new UserException("Connection is null, object could not be created");
		}
		
		PreparedStatement akvSecretSt;
		PreparedStatement cardSt;
		ResultSet rs = null;
		int akvSecretId = 0;			
			
		try {
			String insertAkvSecretQuery = "insert into akvsecrets (akv_secret_name, state) values (?, ?) returning akv_secret_id";
			akvSecretSt = conn.prepareStatement(insertAkvSecretQuery);
			akvSecretSt.setString(1, akvSecretName);
			akvSecretSt.setString(2, DatabaseConstants.ACTIVE);
			rs = akvSecretSt.executeQuery();
			if(rs.next())
				akvSecretId = rs.getInt("akv_secret_id");			
		} 
		catch(Exception e) {
			throw new UserException("Exception occured while creating Akv Secret record", e);
		}
				
		try {
			String insertCardQuery = "insert into cards (user_id, akv_secret_id, state) values (?, ?, ?)";
			cardSt = conn.prepareStatement(insertCardQuery);
			cardSt.setInt(1, userId);
			cardSt.setInt(2, akvSecretId);
			cardSt.setString(3, DatabaseConstants.ACTIVE);
			cardSt.executeUpdate();
		} 
		catch(Exception e) {
			throw new UserException("Exception occured while creating Card record", e);
		}		
	}

	/**
	 * Updates the state of both the AKV secret and associated card record to INACTIVE in the database.
	 *
	 * Steps:
	 * - Updates the state of the AKV secret to "INACTIVE".
	 * - Retrieves the AKV secret ID from the database.
	 * - Updates the state of the card record linked to that AKV secret ID to "INACTIVE".
	 *
	 * @param userId         The user ID whose records are to be updated.
	 * @param akvSecretName  The name of the AKV secret to be marked as inactive.
	 * @throws CreditCardException if any error occurs during the update process.
	 */
	public static void updateCardAndSecretMetadataToInactive(int userId, String akvSecretName) throws CreditCardException {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new CreditCardException("Connection is null, object could not be created");
		}
		PreparedStatement st;
		int akvSecretId = 0;
		
		try {
			String query = "update akvsecrets set state = ? where akv_secret_name = ?";
			st = conn.prepareStatement(query);
			st.setString(1, DatabaseConstants.INACTIVE);
			st.setString(2, akvSecretName);
			st.executeUpdate();			
		} 
		catch(Exception e) {
			throw new CreditCardException("Exception occured while updating akvsecret record as inactive", e);
		}
		
		try {
			String query = "select akv_secret_id from akvsecrets where akv_secret_name = ?";
			st = conn.prepareStatement(query);
			st.setString(1, akvSecretName);
		    ResultSet rs = st.executeQuery();
		    if (rs.next()) {
		        akvSecretId = rs.getInt("akv_secret_id");
		    } 
		} 
		catch(Exception e) {
			throw new CreditCardException("Exception occured while retrieving akvsecretid", e);
		}
		
		try {
			String query = "update cards set state = ? where akv_secret_id = ?";
			st = conn.prepareStatement(query);
			st.setString(1, DatabaseConstants.INACTIVE);
			st.setInt(2, akvSecretId);
			st.executeUpdate();		
		} 
		catch(Exception e) {
			throw new CreditCardException("Exception occured while updating card record as inactive", e);
		}								
	}	
}