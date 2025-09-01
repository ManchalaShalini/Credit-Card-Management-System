package com.creditcard.utils;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import com.creditcard.constants.AkvConstants;

/**
 * Utility class for interacting with Azure Key Vault (AKV).
 */
@Component
public class AkvSecretHelper {
    
	/**
     * Builds and returns a SecretClient instance to interact with a given Azure Key Vault.
     *
     * @param akvName The name of the Azure Key Vault.
     * @return A SecretClient instance for the specified Key Vault.
     */
    public SecretClient getSecretClient(String akvName) {
		String keyVaultURL = String.format(AkvConstants.keyVaultUrlTemplate, akvName);
		return new SecretClientBuilder().vaultUrl(keyVaultURL).credential(new DefaultAzureCredentialBuilder().build()).buildClient();		
	}

    /**
     * Generates a unique secret name by appending a random UUID to a predefined prefix.
     *
     * @return A new secret name.
     */
	public String generateSecretName() {		
		return AkvConstants.akvSecretNamePrefix + UUID.randomUUID().toString();
	}
}
