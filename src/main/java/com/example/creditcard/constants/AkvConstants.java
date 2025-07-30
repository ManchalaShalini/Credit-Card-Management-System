package com.example.creditcard.constants;

/**
 * Utility class that holds constant values related to Azure Key Vault (AKV).
 */ 
public class AkvConstants {
	public static final String akvSecretNamePrefix = "creditcard-";
	public static final String akvName = "creditcard-keyvault";
	public static final String databaseUserSecretName = "ccms-db-username";
	public static final String databasePasswordSecretName = "ccms-db-password";
	public static final String keyVaultUrlTemplate = "https://%s.vault.azure.net";
}
	