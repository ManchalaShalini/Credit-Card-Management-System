package com.example.creditcard.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.azure.security.keyvault.secrets.SecretClient;
import com.example.creditcard.akv.CreditCardAkvSecretHandler;
import com.example.creditcard.constants.AkvConstants;
import com.example.creditcard.constants.DatabaseConstants;
import com.example.creditcard.utils.AkvSecretHelper;
import com.example.creditcard.utils.DatabaseHelper;

import jakarta.annotation.PostConstruct;

/**
 * This class is responsible for initializing, creating, and dropping database tables 
 * for the Credit Card Management System. It also creates database if it not exists during application start.
 */
@Component
public class DatabaseInitializer {
	
private String dbUrl = DatabaseConstants.dbUrl;

private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

	
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
     * Creates required PostgreSQL database tables and sequences if they do not already exist.
     * 
     * @throws Exception if a database connection cannot be established
     */
    public void createTables() throws Exception {
    	Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if (conn == null)
		{
			throw new Exception("Connection is null, object could not be created");
		}
		
		try {
			Statement st = conn.createStatement();
			String createSequence = "CREATE SEQUENCE IF NOT EXISTS \"User_userId_seq\" START 1;";
			st.executeUpdate(createSequence);
			
			String createQuery = "CREATE TABLE IF NOT EXISTS public.users\r\n"
					+ "(\r\n"
					+ "    user_id integer NOT NULL DEFAULT nextval('\"User_userId_seq\"'::regclass),\r\n"
					+ "    user_name text COLLATE pg_catalog.\"default\" NOT NULL,\r\n"
					+ "    email_address text COLLATE pg_catalog.\"default\" NOT NULL,\r\n"
					+ "    created_at timestamp without time zone DEFAULT now(),\r\n"
					+ "    modified_at timestamp without time zone DEFAULT now(),\r\n"
					+ "    state text COLLATE pg_catalog.\"default\" NOT NULL,\r\n"
					+ "    CONSTRAINT \"User_pkey\" PRIMARY KEY (user_id)\r\n"
					+ ")";
			st.executeUpdate(createQuery);
			
			createSequence = "CREATE SEQUENCE IF NOT EXISTS \"AKVSecret_akvSecretId_seq\" START 1;";
			st.executeUpdate(createSequence);

			createQuery = "CREATE TABLE IF NOT EXISTS public.akvsecrets\r\n"
					+ "(\r\n"
					+ "    akv_secret_id integer NOT NULL DEFAULT nextval('\"AKVSecret_akvSecretId_seq\"'::regclass),\r\n"
					+ "    akv_secret_name text COLLATE pg_catalog.\"default\" NOT NULL,\r\n"
					+ "    created_at timestamp without time zone DEFAULT now(),\r\n"
					+ "    modified_at timestamp without time zone DEFAULT now(),\r\n"
					+ "    state text COLLATE pg_catalog.\"default\" NOT NULL,\r\n"
					+ "    CONSTRAINT \"AKVSecret_pkey\" PRIMARY KEY (akv_secret_id)\r\n"
					+ ")";
			st.executeUpdate(createQuery);
			
			createSequence = "CREATE SEQUENCE IF NOT EXISTS \"Card_cardId_seq\" START 1;";
			st.executeUpdate(createSequence);
			
			createSequence = "CREATE SEQUENCE IF NOT EXISTS \"Card_userId_seq\" START 1;";
			st.executeUpdate(createSequence);
			
			createSequence = "CREATE SEQUENCE IF NOT EXISTS \"Card_akvSecretId_seq\" START 1;";
			st.executeUpdate(createSequence);
						
			createQuery = "CREATE TABLE IF NOT EXISTS public.cards\r\n"
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
			st.executeUpdate(createQuery);
						
			logger.info("Tables created successfully or already exists");
		} 
		catch(Exception e) {
			logger.error("Exception occured while creating tables", e);
		}
    }

    /**
     * Drops all database tables and associated AKV secrets.
     *
     * @throws Exception if the database connection fails
     */
	public void dropTables() throws Exception {
		Connection conn = DatabaseHelper.connectToDB(DatabaseConstants.databaseName, dbUser, dbPwd, dbUrl);
		if (conn == null)
		{
			throw new Exception("Connection is null, object could not be created");
		}
		
		try {
			Statement st = conn.createStatement();
			String query = "DROP TABLE IF EXISTS public.cards";
			st.executeUpdate(query);
			
			List<String> akvSecrets = CreditCardDao.getActiveAkvSecrets();
			CreditCardAkvSecretHandler.deleteAkSecrets(AkvConstants.akvName, akvSecrets);			
			
			query = "DROP TABLE IF EXISTS public.akvsecrets;";
			st.executeUpdate(query);
			
			query = "DROP TABLE IF EXISTS public.users";
			st.executeUpdate(query);
			
            logger.info("Tables dropped");
		} 
		catch(Exception e) {
			logger.error("Exception occured while dropping tables", e);
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
			throw new Exception("Connection is null, object could not be created");
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
			logger.info("Exception while check if database exists", ex);
		}
	}
}
