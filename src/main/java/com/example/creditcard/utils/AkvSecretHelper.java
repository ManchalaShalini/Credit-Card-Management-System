package com.example.creditcard.utils;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.example.creditcard.constants.AkvConstants;

/**
 * Utility class for interacting with Azure Key Vault (AKV).
 * 
 * This helper provides methods to create a SecretClient as well as generate
 * random secret names with a predefined prefix.
 *
 */
@Component
public class AkvSecretHelper {
    
	/**
     * Builds and returns a SecretClient instance to interact with a given Azure Key Vault.
     *
     * @param akvName The name of the Azure Key Vault (without URL or protocol).
     * @return A configured {@link SecretClient} for the specified Key Vault.
     */
    public static SecretClient getSecretClient(String akvName) {
		String keyVaultURL = String.format(AkvConstants.keyVaultUrlTemplate, akvName);
		return new SecretClientBuilder().vaultUrl(keyVaultURL).credential(new DefaultAzureCredentialBuilder().build()).buildClient();		
	}

    /**
     * Generates a unique secret name by appending a random 8-character UUID substring
     * to a predefined prefix.
     *
     * @return A new secret name string.
     */
	public static String generateSecretName() {		
		return AkvConstants.akvSecretNamePrefix + UUID.randomUUID().toString().substring(0, 8);
	}
}
