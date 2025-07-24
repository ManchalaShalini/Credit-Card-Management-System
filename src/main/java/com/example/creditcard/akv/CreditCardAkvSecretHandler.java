package com.example.creditcard.akv;

import java.util.ArrayList;
import java.util.List;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.example.creditcard.model.CreditCard;
import com.example.creditcard.utils.AkvSecretHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class CreditCardAkvSecretHandler {
    private final static ObjectMapper objectMapper = new ObjectMapper();		
    
    public static void storeCardDetails(CreditCard creditCardRequest, String akvName, String akvSecretName) throws Exception {
		String cardRequestJson;
		try {
			SecretClient client = AkvSecretHelper.getSecretClient(akvName);

			cardRequestJson = objectMapper.writeValueAsString(creditCardRequest);
			client.setSecret(akvSecretName, cardRequestJson);
		} catch (Exception e) {
			throw new Exception("Error while storing card details in Akv secret", e);
		}
	}
    
	public static void updateCardDetails(CreditCard creditCardRequest, String akvName, List<String> akvSecrets) throws Exception {
		String cardRequestJson;
		try {
			SecretClient client = AkvSecretHelper.getSecretClient(akvName);
			for(String akvSecretName : akvSecrets) {
				KeyVaultSecret currentSecret = client.getSecret(akvSecretName);
		        String secretJson = currentSecret.getValue();
		        
		        ObjectMapper mapper = new ObjectMapper();
		        JsonNode rootNode = mapper.readTree(secretJson);

		        String secretCardNumber = rootNode.path("cardNumber").asText();

		        if (secretCardNumber.equals(creditCardRequest.getCardNumber())) {		  
					cardRequestJson = objectMapper.writeValueAsString(creditCardRequest);
					client.setSecret(akvSecretName, cardRequestJson);
					break;
		        }
			}
		} catch (Exception e) {
			throw new Exception("Error while storing card details in Akv secret", e);
		}
	}
	
	public static void deleteCardDetails(String akvName, List<String> akvSecrets, String cardNumber) throws Exception {
		try {
			SecretClient client = AkvSecretHelper.getSecretClient(akvName);
			for(String akvSecretName : akvSecrets) {
		        KeyVaultSecret currentSecret = client.getSecret(akvSecretName);
		        String secretJson = currentSecret.getValue();
		        
		        ObjectMapper mapper = new ObjectMapper();
		        JsonNode rootNode = mapper.readTree(secretJson);

		        String secretCardNumber = rootNode.path("cardNumber").asText();

		        if (secretCardNumber.equals(cardNumber)) {		        
		        	client.beginDeleteSecret(akvSecretName);
		        	break;
		        }
			}
		} catch (Exception e) {
			throw new Exception("Error while deleting card details from Akv secret", e);
		}
	}
	
	public static List<String> getCardDetailsByUser(String akvName, List<String> akvSecrets) throws Exception {
		List<String> cardDetails = new ArrayList<>();
		try {
			SecretClient client = AkvSecretHelper.getSecretClient(akvName);
			for(String akvSecretName : akvSecrets) {
				String secretJson = client.getSecret(akvSecretName).getValue();
				cardDetails.add(secretJson);
			}
		} catch (Exception e) {
			throw new Exception("Error while retrieving card details from Akv secret", e);
		}
		return cardDetails;
	}
}
