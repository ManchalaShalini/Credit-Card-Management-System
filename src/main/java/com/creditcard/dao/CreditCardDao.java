package com.creditcard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.azure.security.keyvault.secrets.SecretClient;
import com.creditcard.constants.AkvConstants;
import com.creditcard.constants.DatabaseConstants;
import com.creditcard.exceptions.CreditCardException;
import com.creditcard.exceptions.UserException;
import com.creditcard.utils.AkvSecretHelper;
import com.creditcard.utils.DatabaseHelper;

import jakarta.annotation.PostConstruct;

/**
 * Data Access Object (DAO) class for managing operations related to credit card metadata.
 *
 * This class includes logic to:
 * - Retrieve AKV secret names associated with a user.
 * - Store new card and secret metadata entries.
 * - Mark card and secret entries as inactive.
 */
@Component
public class CreditCardDao {
	
	@Autowired
	private AkvSecretHelper akvSecretHelper;
	
	@Autowired
	private DatabaseHelper databaseHelper;

	private String dbUrl;	
	private String dbUser;
    private String dbPwd;

    /**
     * Initializes database credentials from Azure Key Vault.
     * This method is automatically executed after bean construction due to @PostConstruct annotation.
     */
    @PostConstruct
    private void initSecrets() {
        SecretClient client = akvSecretHelper.getSecretClient(AkvConstants.akvName);
        dbUrl = DatabaseConstants.dbUrl;
        dbUser = client.getSecret(AkvConstants.databaseUserSecretName).getValue();
        dbPwd = client.getSecret(AkvConstants.databasePasswordSecretName).getValue();
    }

    /**
     * Retrieves a list of active AKV (Azure Key Vault) secret names linked to a specific user.
     * @param userId The ID of the user.
     * @param state State of the AKV record.
     * @return A list of AKV secret names associated with the user.
     * @throws UserException if the database connection fails or an SQL error occurs.
     */	
	public List<String> getAkvSecretsByUserId(int userId, String state) throws UserException{
		Connection conn = databaseHelper.getConnection(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
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
			st.setString(2, state);
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
	 * Steps:
	 * - Inserts a new AKV secret entry.
	 * - Inserts a new card entry referencing the created AKV secret.
	 *
	 * @param userId         The user ID for whom the card is being stored.
	 * @param akvSecretName  The name of the AKV secret used to store the card securely.
	 * @throws UserException if any error occurs during database operations.
	 */
	public void storeCardAndSecretMetadata(int userId, String akvSecretName) throws UserException {
		Connection conn = databaseHelper.getConnection(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new UserException("Connection is null, object could not be created");
		}
		
		PreparedStatement akvSecretSt;
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
		
		PreparedStatement cardSt;
		try {
			String insertCardQuery = "insert into cards (user_id, akv_secret_id, state) values (?, ?, ?)";
			cardSt = conn.prepareStatement(insertCardQuery);
			cardSt.setInt(1, userId);
			cardSt.setInt(2, akvSecretId);
			cardSt.setString(3, DatabaseConstants.ACTIVE);
			cardSt.executeUpdate();
		} 
		catch(Exception e) {
			// TODO: need to handle case if first insert succeeds and 2nd one fails
			throw new UserException("Exception occured while creating Card record", e);
		}		
	}

	/**
	 * Updates the state of both the AKV secret and associated card record to INACTIVE in the database.
	 * Steps:
	 * - Updates the state of the AKV secret to "INACTIVE".
	 * - Retrieves the AKV secret ID from the database.
	 * - Updates the state of the card record linked to that AKV secret ID to "INACTIVE".
	 *
	 * @param userId         The user ID whose records are to be updated.
	 * @param akvSecretName  The name of the AKV secret to be marked as inactive.
	 * @param state 
	 * @throws CreditCardException if any error occurs during the update process.
	 */
	public void updateCardAndSecretMetadata(int userId, String akvSecretName, String state) throws CreditCardException {
		Connection conn = databaseHelper.getConnection(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if(conn == null) {
			throw new CreditCardException("Connection is null, object could not be created");
		}
		PreparedStatement st;
		int akvSecretId = 0;
		
		try {			
			String query = "update akvsecrets set state = ?, modified_at = ? where akv_secret_name = ?";
			st = conn.prepareStatement(query);
			st.setString(1, state);
			st.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
			st.setString(3, akvSecretName);
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
			String query = "update cards set state = ?, modified_at = ? where akv_secret_id = ?";
			st = conn.prepareStatement(query);
			st.setString(1, state);
			st.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
			st.setInt(3, akvSecretId);
			st.executeUpdate();		
		} 
		catch(Exception e) {
			// TODO: handle case if first update succeeds and this fails
			throw new CreditCardException("Exception occured while updating card record as inactive", e);
		}								
	}	
}