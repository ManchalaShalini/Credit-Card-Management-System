package com.example.creditcard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.creditcard.constants.DatabaseConstants;
import com.example.creditcard.dao.DatabaseInitializer;

/**
 * Entry point for the Credit Card Management System Spring Boot application.
 * 
 * This class bootstraps the Spring Boot application using Spring Boot's auto-configuration 
 * and component scanning capabilities. The application context is created and managed 
 * by Spring at runtime.
 * 
 * Annotations used:
 * - @SpringBootApplication: Combines @Configuration, @EnableAutoConfiguration, and 
 *   @ComponentScan. It indicates that this is the primary configuration class and 
 *   triggers Spring Boot’s auto-configuration and component scanning.
 */
@SpringBootApplication
public class CreditCardManagementSystemApplication implements CommandLineRunner {
	
	@Autowired
    private DatabaseInitializer databaseInitializer;

	/**
     * Main method — the entry point of the Java application.
     * 
     * This method delegates to Spring Boot’s SpringApplication.run() method to 
     * launch the application. It sets up the default configuration, starts 
     * the Spring application context, and performs classpath scanning.
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
//		databaseInitializer.dropTables();	
	}

}
