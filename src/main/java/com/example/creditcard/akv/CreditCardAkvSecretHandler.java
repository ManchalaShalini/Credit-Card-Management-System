package com.example.creditcard.akv;

import java.util.ArrayList;
import java.util.List;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.example.creditcard.model.CreditCard;
import com.example.creditcard.utils.AkvSecretHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handler class to interact with Azure Key Vault for storing, updating, retrieving, 
 * and deleting credit card information stored as secrets.
 * 
 * Utilizes Azure Key Vault SDK to securely manage credit card data. Converts `CreditCard` 
 * Java objects to and from JSON when interacting with secrets.
 */
public class CreditCardAkvSecretHandler {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Stores a new credit card detail in Azure Key Vault as a secret.
     *
     * @param creditCardRequest The credit card information to store.
     * @param akvName           The name of the Azure Key Vault.
     * @param akvSecretName     The name under which the secret will be stored.
     * @throws Exception if there is an error during the storing process.
     */
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
    
    /**
     * Updates existing credit card information in Azure Key Vault.
     * 
     * The method iterates over a list of secret names, retrieves each, and compares 
     * the stored card number to the one in the incoming request. If a match is found, 
     * the secret is updated with the new card details.
     *
     * @param creditCardRequest The updated credit card information.
     * @param akvName           The name of the Azure Key Vault.
     * @param akvSecrets        List of secret names to search for the card number.
     * @throws Exception if an error occurs during update.
     */
	public static void updateCardDetails(CreditCard creditCardRequest, String akvName, List<String> akvSecrets) throws Exception {
		String cardRequestJson;
		try {
		    SecretClient client = AkvSecretHelper.getSecretClient(akvName);
		    for (String akvSecretName : akvSecrets) {
		        KeyVaultSecret currentSecret = client.getSecret(akvSecretName);
		        String secretJson = currentSecret.getValue();

		        ObjectMapper mapper = new ObjectMapper();
		        JsonNode rootNode = mapper.readTree(secretJson);
		        String secretCardNumber = rootNode.path("cardNumber").asText();

			    if (secretCardNumber.equals(creditCardRequest.getCardNumber())) {		  
		            cardRequestJson = mapper.writeValueAsString(creditCardRequest);
		            client.setSecret(akvSecretName, cardRequestJson);
		            break;
		        }		        
		    }
		} catch (Exception e) {
		    throw new Exception("Error while updating card details in AKV", e);
		}
	}
	
	 /**
     * Deletes a credit card record from Azure Key Vault by matching the card number.
     *
     * Iterates through the given list of secrets and deletes the one that matches 
     * the provided card number.
     *
     * @param akvName     The name of the Azure Key Vault.
     * @param akvSecrets  List of secret names to search.
     * @param cardNumber  The card number to identify the secret to delete.
     * @return The name of the deleted secret if a match was found; otherwise null.
     * @throws Exception if an error occurs during deletion.
     */
	public static String deleteCardDetails(String akvName, List<String> akvSecrets, String cardNumber) throws Exception {
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
			        return akvSecretName;			   		        
			    }
			}
		} catch (Exception e) {
			throw new Exception("Error while deleting card details from Akv secret", e);
		}
		return null;
	}
	
	/**
     * Retrieves all credit card details for a user from Azure Key Vault.
     *
     * Parses each secret in the provided list and extracts relevant credit card 
     * information (card number and expiry date).
     *
     * @param akvName     The name of the Azure Key Vault.
     * @param akvSecrets  List of secret names to retrieve and parse.
     * @return List of CreditCard objects corresponding to the retrieved secrets.
     * @throws Exception if retrieval or parsing fails.
     */
	public static List<CreditCard> getCardDetailsByUser(String akvName, List<String> akvSecrets, int userId) throws Exception {
		List<CreditCard> cardDetails = new ArrayList<>();
		try {
			SecretClient client = AkvSecretHelper.getSecretClient(akvName);
			for(String akvSecretName : akvSecrets) {
				String secretJson = client.getSecret(akvSecretName).getValue();
					
				ObjectMapper mapper = new ObjectMapper();
			    JsonNode rootNode = mapper.readTree(secretJson);
			        
				CreditCard cardResponse = new CreditCard();
				cardResponse.setUserID(userId);
				cardResponse.setCardNumber(rootNode.path("cardNumber").asText());
				cardResponse.setExpiryDate(rootNode.path("expiryDate").asText());
				cardDetails.add(cardResponse);								
			}
		} catch (Exception e) {
			throw new Exception("Error while retrieving card details from Akv secret", e);
		}
		return cardDetails;
	}
}
