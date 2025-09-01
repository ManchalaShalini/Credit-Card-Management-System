package com.creditcard.akv;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.creditcard.model.CreditCard;
import com.creditcard.model.CreditCardVault;
import com.creditcard.utils.AkvSecretHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handler class to interact with Azure Key Vault for storing, updating, retrieving and deleting credit card information stored as secrets.
 * 
 * Utilizes Azure Key Vault SDK to securely manage credit card data. Converts CreditCard Java objects to and from JSON when 
 * interacting with secrets.
 */
@Component
public class CreditCardAkvSecretHandler {
	@Autowired 
	private AkvSecretHelper akvSecretHelper;
	
    private final static ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Stores new credit card details in Azure Key Vault as a secret.
     *
     * @param creditCard The credit card information to store.
     * @param akvName           The name of the Azure Key Vault.
     * @param akvSecretName     The name under which the secret will be stored.
     * @throws Exception if there is an error during the storing process.
     */
    public void storeCard(CreditCard creditCard, String akvName, String akvSecretName) throws Exception {
		String cardRequestJson;
		try {
			SecretClient client = akvSecretHelper.getSecretClient(akvName);
			
			CreditCardVault creditcardvault = new CreditCardVault();
			creditcardvault.setCardNumber(creditCard.getCardNumber());
			creditcardvault.setExpiryDate(creditCard.getExpiryDate());
			
			cardRequestJson = objectMapper.writeValueAsString(creditcardvault); // creditCardvault object holds only card number and expiry details.
			client.setSecret(akvSecretName, cardRequestJson);
		} catch (Exception e) {
			throw new Exception("Error while storing card details in Akv secret", e);
		}
	}
    
    /**
     * Updates existing credit card information in Azure Key Vault.
     * 
     * The method iterates through secrets list, compares card numbers, and updates the matching secret with new details.
     *
     * @param updatedCreditCard The updated credit card information.
     * @param akvName           The name of the Azure Key Vault.
     * @param akvSecrets        List of secret names to search.
     * @throws Exception if an error occurs during update.
     */
	public void updateCard(CreditCard updatedCreditCard, String akvName, List<String> akvSecrets) throws Exception {
		String cardRequestJson;
		try {
		    SecretClient client = akvSecretHelper.getSecretClient(akvName);
		    for (String akvSecretName : akvSecrets) {
		        ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = parseSecretJSON(akvSecretName,client,mapper);

		        String secretCardNumber = rootNode.path("cardNumber").asText();
		        
		        CreditCardVault creditcardvault = new CreditCardVault();
				creditcardvault.setCardNumber(updatedCreditCard.getCardNumber());
				creditcardvault.setExpiryDate(updatedCreditCard.getExpiryDate());
				
			    if (secretCardNumber.equals(updatedCreditCard.getCardNumber())) {		  
		            cardRequestJson = mapper.writeValueAsString(creditcardvault);
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
     * Iterates through the secrets list, compares card numbers, and deletes the secret that matches the provided card number.
     *
     * @param akvName     The name of the Azure Key Vault.
     * @param akvSecrets  List of secret names to search.
     * @param cardNumber  The card number to identify the secret to delete.
     * @return The name of the deleted secret if a match was found; otherwise null.
     * @throws Exception if an error occurs during deletion.
     */
	public String deleteCard(String akvName, List<String> akvSecrets, String cardNumber) throws Exception {
		try {
			SecretClient client = akvSecretHelper.getSecretClient(akvName);
			for(String akvSecretName : akvSecrets) {
		        ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = parseSecretJSON(akvSecretName,client,mapper);
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
     * Iterates through the secrets list and extracts relevant credit card information (card number and expiry date).
     *
     * @param akvName     The name of the Azure Key Vault.
     * @param akvSecrets  List of secret names to retrieve and parse.
     * @return List of CreditCard objects corresponding to the retrieved secrets.
     * @throws Exception if retrieval or parsing fails.
     */
	public List<CreditCardVault> getCardsByUser(String akvName, List<String> akvSecrets, int userId) throws Exception {
		List<CreditCardVault> cardSecretDetails = new ArrayList<>();
		try {
			SecretClient client = akvSecretHelper.getSecretClient(akvName);
			for(String akvSecretName : akvSecrets) {
		        ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = parseSecretJSON(akvSecretName,client,mapper);
			        
				CreditCardVault cardResponse = new CreditCardVault();
				cardResponse.setCardNumber(rootNode.path("cardNumber").asText());
				cardResponse.setExpiryDate(rootNode.path("expiryDate").asText());
				cardSecretDetails.add(cardResponse);								
			}
		} catch (Exception e) {
			throw new Exception("Error while retrieving card details from Akv secret", e);
		}
		return cardSecretDetails;
	}	
	
	private JsonNode parseSecretJSON(String akvSecretName, SecretClient client, ObjectMapper mapper) throws JsonMappingException, JsonProcessingException {
		KeyVaultSecret currentSecret = client.getSecret(akvSecretName);
		String secretJson = currentSecret.getValue();
	        
		JsonNode rootNode = mapper.readTree(secretJson);
	    return rootNode;
	}
}