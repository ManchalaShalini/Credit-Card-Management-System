package com.example.creditcard.utils;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.example.creditcard.constants.AkvConstants;

@Component
public class AkvSecretHelper {
    
    public static SecretClient getSecretClient(String akvName) {
		String keyVaultURL = String.format(AkvConstants.keyVaultUrlTemplate, akvName);
		return new SecretClientBuilder().vaultUrl(keyVaultURL).credential(new DefaultAzureCredentialBuilder().build()).buildClient();		
	}

	public static String generateSecretName() {		
		return AkvConstants.akvSecretNamePrefix + UUID.randomUUID().toString().substring(0, 8);
	}
}
