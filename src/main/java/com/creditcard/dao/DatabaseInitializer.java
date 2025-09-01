package com.creditcard.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.azure.security.keyvault.secrets.SecretClient;
import com.creditcard.constants.AkvConstants;
import com.creditcard.constants.DatabaseConstants;
import com.creditcard.utils.AkvSecretHelper;
import com.creditcard.utils.DatabaseHelper;

import jakarta.annotation.PostConstruct;

/**
 * This class handles the initialization of the database and creates the necessary tables for the Credit Card Management System if they 
 * do not already exist.
 */
@Component
public class DatabaseInitializer {
	@Autowired
	private AkvSecretHelper akvSecretHelper;
	@Autowired
	private DatabaseHelper databaseHelper;
	
	private String dbUrl;
	private String dbUser;
    private String dbPwd;
    
    private Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
   
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
     * Creates required PostgreSQL database tables and sequences if they do not already exist.
     * 
     * @throws Exception if a connection or query fails
     */
    public void createTables() throws Exception {
    	Connection conn = databaseHelper.getConnection(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if (conn == null)
		{
			throw new Exception("Connection is null, could not establish connection to DB");
		}
		
		try {
			Statement st = conn.createStatement();
			String createUserSeq = "CREATE SEQUENCE IF NOT EXISTS \"User_userId_seq\" START 1;";
			st.executeUpdate(createUserSeq);
			String createUserTable = constructCreateUserTableQuery();
			st.executeUpdate(createUserTable);
			
			String createAkvSecretSequence = "CREATE SEQUENCE IF NOT EXISTS \"AKVSecret_akvSecretId_seq\" START 1;";
			st.executeUpdate(createAkvSecretSequence);

			String createAkvsecretTable = constructCreateAkvsecretTableQuery();
			st.executeUpdate(createAkvsecretTable);
			
			String createCardSequence = "CREATE SEQUENCE IF NOT EXISTS \"Card_cardId_seq\" START 1;";
			st.executeUpdate(createCardSequence);
			
			String cardUserIdSeq = "CREATE SEQUENCE IF NOT EXISTS \"Card_userId_seq\" START 1;";
			st.executeUpdate(cardUserIdSeq);
			
			String cardAkvSecretIdSeq = "CREATE SEQUENCE IF NOT EXISTS \"Card_akvSecretId_seq\" START 1;";
			st.executeUpdate(cardAkvSecretIdSeq);
				
			String createCardTable = constructCreateCardTableQuery();
			st.executeUpdate(createCardTable);
						
			logger.info("Tables created successfully or already exists");
		} 
		catch(Exception e) {
			logger.error("Exception occured while creating tables", e);
			throw e;
		}
    }

	/**
     * Creates the specified database if it doesn't already exist.
     *
     * @param dbName The name of the database to create
     * @throws Exception if a connection or query fails
     */
	public void createDatabaseIfNotExists(String dbName) throws Exception {
		Connection conn = DriverManager.getConnection(DatabaseConstants.dbUrl+"postgres", dbUser, dbPwd);
		if (conn == null)
		{
			throw new Exception("Connection is null, could not establish connection to DB");
		}
		
		Statement st = conn.createStatement();
		try {
			 String query = "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'";
	         ResultSet rs = st.executeQuery(query);

	         if (!rs.next()) {	        	 
	        	 query = "CREATE DATABASE " + dbName;
	             st.executeUpdate(query);
	             logger.info("Database created: " + dbName);
	         } else {
	        	 logger.info("Database already exists: " + dbName);
	         }
		} catch (Exception ex) {
			logger.info("Exception while checking database existence or creating db", ex);
		}
	}
	
	private String constructCreateUserTableQuery() {
		return "CREATE TABLE IF NOT EXISTS public.users\r\n"
				+ "(\r\n"
				+ "    user_id integer NOT NULL DEFAULT nextval('\"User_userId_seq\"'::regclass),\r\n"
				+ "    user_name text COLLATE pg_catalog.\"default\" NOT NULL,\r\n"
				+ "    email_address text COLLATE pg_catalog.\"default\" NOT NULL,\r\n"
				+ "    created_at timestamp without time zone DEFAULT now(),\r\n"
				+ "    modified_at timestamp without time zone DEFAULT now(),\r\n"
				+ "    state text COLLATE pg_catalog.\"default\" NOT NULL,\r\n"
				+ "    CONSTRAINT \"User_pkey\" PRIMARY KEY (user_id)\r\n"
				+ ")";
	}
	
	private String constructCreateAkvsecretTableQuery() {
		return "CREATE TABLE IF NOT EXISTS public.akvsecrets\r\n"
				+ "(\r\n"
				+ "    akv_secret_id integer NOT NULL DEFAULT nextval('\"AKVSecret_akvSecretId_seq\"'::regclass),\r\n"
				+ "    akv_secret_name text COLLATE pg_catalog.\"default\" NOT NULL,\r\n"
				+ "    created_at timestamp without time zone DEFAULT now(),\r\n"
				+ "    modified_at timestamp without time zone DEFAULT now(),\r\n"
				+ "    state text COLLATE pg_catalog.\"default\" NOT NULL,\r\n"
				+ "    CONSTRAINT \"AKVSecret_pkey\" PRIMARY KEY (akv_secret_id)\r\n"
				+ ")";
	}
	
	private String constructCreateCardTableQuery() {
		return "CREATE TABLE IF NOT EXISTS public.cards\r\n"
				+ "(\r\n"
				+ "    card_id integer NOT NULL DEFAULT nextval('\"Card_cardId_seq\"'::regclass),\r\n"
				+ "    user_id integer NOT NULL DEFAULT nextval('\"Card_userId_seq\"'::regclass),\r\n"
				+ "    akv_secret_id integer NOT NULL DEFAULT nextval('\"Card_akvSecretId_seq\"'::regclass),\r\n"
				+ "    created_at timestamp without time zone DEFAULT now(),\r\n"
				+ "    modified_at timestamp without time zone DEFAULT now(),\r\n"
				+ "    state text COLLATE pg_catalog.\"default\" NOT NULL,\r\n"
				+ "    CONSTRAINT \"Card_pkey\" PRIMARY KEY (card_id),\r\n"
				+ "    CONSTRAINT \"fk_akvSecret\" FOREIGN KEY (akv_secret_id)\r\n"
				+ "        REFERENCES public.akvsecrets (akv_secret_id) MATCH SIMPLE\r\n"
				+ "        ON UPDATE CASCADE\r\n"
				+ "        ON DELETE RESTRICT\r\n"
				+ "        NOT VALID,\r\n"
				+ "    CONSTRAINT fk_user FOREIGN KEY (user_id)\r\n"
				+ "        REFERENCES public.users (user_id) MATCH SIMPLE\r\n"
				+ "        ON UPDATE CASCADE\r\n"
				+ "        ON DELETE RESTRICT\r\n"
				+ ")\r\n"
				+ "";
	}
}
