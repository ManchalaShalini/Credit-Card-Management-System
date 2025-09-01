package com.creditcard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.creditcard.constants.DatabaseConstants;
import com.creditcard.dao.DatabaseInitializer;

/**
 * Main entry point for the Credit Card Management System Spring Boot application. 
 * This class starts the Spring Boot application and sets up everything that is needed to run it.
 * 
 * Annotations used:
 * - @SpringBootApplication: Marks this class as Main class. It tells Spring Boot to automatically configure the app and scan for components.
 */
@SpringBootApplication
public class CreditCardManagementSystemApplication implements CommandLineRunner {
	
	@Autowired
    private DatabaseInitializer databaseInitializer;

	/**
     * Main method that calls SpringApplication.run() method to start the application. It sets up the default configuration and gets 
     * everything running.
     *
     * @param args Command-line arguments passed during application startup.
     */
	public static void main(String[] args) {
		SpringApplication.run(CreditCardManagementSystemApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		databaseInitializer.createDatabaseIfNotExists(DatabaseConstants.databaseName);
		databaseInitializer.createTables();
	}
}
