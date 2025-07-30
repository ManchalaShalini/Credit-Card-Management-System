package com.example.creditcard.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.creditcard.akv.CreditCardAkvSecretHandler;
import com.example.creditcard.constants.AkvConstants;
import com.example.creditcard.constants.CreditCardControllerConstants;
import com.example.creditcard.dao.CreditCardDao;
import com.example.creditcard.exceptions.CreditCardException;
import com.example.creditcard.model.CreditCard;
import com.example.creditcard.utils.AkvSecretHelper;
import com.example.creditcard.utils.CardValidationHelper;
import com.example.creditcard.utils.ControllerHelper;

/**
 * REST Controller for managing credit card information.
 * 
 * This controller provides endpoints to:
 * - Store new credit card details
 * - Update existing card details
 * - Delete card information
 * - Retrieve card details by user
 * - Validate credit card information
 * 
 * All sensitive card data is securely stored in Azure Key Vault, and metadata is maintained in the database.
 */
@RestController
@RequestMapping("/api")
public class CreditCardController {
		 
	 /**
	  * Stores credit card details in Azure Key Vault and metadata in DB.
	  *
	  * @param cardDetails JSON payload representing the credit card data.
	  * @return ResponseEntity with success or error message.
	  * @throws CreditCardException if validation or storage fails.
	  */
	 @PostMapping("/storeCardDetails")
	 public ResponseEntity<Map<String, Object>> storeCardDetails(@RequestBody(required = false) CreditCard cardDetails) throws CreditCardException {
		 
		Map<String, Object> response = new HashMap<>();
		try {			
			if (cardDetails == null) {
	            response.put("error", "User object is null. Please provide card details in the request body.");
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	        } 
			
			String error = ControllerHelper.validate(cardDetails.getUserID(), CreditCardControllerConstants.USER_ID);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
						
			error = ControllerHelper.validate(cardDetails.getCardNumber(), CreditCardControllerConstants.CARD_NUMBER);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			error = ControllerHelper.validate(cardDetails.getExpiryDate(), CreditCardControllerConstants.EXPIRY_DATE);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			String secretName = AkvSecretHelper.generateSecretName();

			CreditCardDao.storeCardAndSecretMetedataDetails(cardDetails.getUserID(), secretName);
			
			CreditCardAkvSecretHandler.storeCardDetails(cardDetails, AkvConstants.akvName, secretName);
			
		    response.put("message", "Card details stored successfully");
		} catch (Exception e) {
			response.put("error", "Failed to store card details: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
	
	 /**
	     * Updates existing credit card details by matching stored secrets.
	     *
	     * @param cardDetails Updated card information.
	     * @return ResponseEntity indicating result.
	     * @throws CreditCardException if validation or update fails.
	     */
	@PutMapping("/updateCardDetails")
	public ResponseEntity<Map<String, Object>> updateCardDetails(@RequestBody(required = false) CreditCard cardDetails) throws CreditCardException {
	    Map<String, Object> response = new HashMap<>();
		try {
			if (cardDetails == null) {
	            response.put("error", "Card object is null. Please provide card details in the request body.");
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	        } 
			
			String error = ControllerHelper.validate(cardDetails.getUserID(), CreditCardControllerConstants.USER_ID);			
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			error = ControllerHelper.validate(cardDetails.getCardNumber(), CreditCardControllerConstants.CARD_NUMBER);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			error = ControllerHelper.validate(cardDetails.getExpiryDate(), CreditCardControllerConstants.EXPIRY_DATE);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			List<String> akvSecrets = CreditCardDao.getActiveAkvSecretsByUserId(cardDetails.getUserID());
			
			CreditCardAkvSecretHandler.updateCardDetails(cardDetails, AkvConstants.akvName, akvSecrets);
			response.put("message", "Card details updated successfully");
	        return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			response.put("error", "Failed to update Card details: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
     * Deletes a credit card secret from Azure Key Vault and marks it inactive in DB.
     *
     * @param cardDetails Card data used to locate and delete the record.
     * @return ResponseEntity with result message.
     * @throws CreditCardException if deletion fails or card not found.
     */
	@DeleteMapping("/deleteCardDetails")
	public ResponseEntity<Map<String, Object>> deleteCardDetails(@RequestBody(required = false) CreditCard cardDetails) throws CreditCardException {
		Map<String, Object> response = new HashMap<>();
		try {
			if (cardDetails == null) {
	            response.put("error", "Card object is null. Please provide card details in the request body.");
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	        } 
			
			String error = ControllerHelper.validate(cardDetails.getUserID(), CreditCardControllerConstants.USER_ID);			
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			error = ControllerHelper.validate(cardDetails.getCardNumber(), CreditCardControllerConstants.CARD_NUMBER);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			List<String> akvSecrets = CreditCardDao.getActiveAkvSecretsByUserId(cardDetails.getUserID());			
			
			// TODO: delete get secret id from d and use that to delete
			String akvSecretName = CreditCardAkvSecretHandler.deleteCardDetails(AkvConstants.akvName, akvSecrets, cardDetails.getCardNumber());
			if(akvSecretName == null) {
				response.put("message", "Card details does not exist in our system");
		        return new ResponseEntity<>(response, HttpStatus.OK);
			}
			
			CreditCardDao.updateCardAndSecretMetadataToInactive(cardDetails.getUserID(), akvSecretName);
						
			response.put("message", "Card details deleted successfully");
	        return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.put("error", "Failed to delete Card details: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	 /**
     * Fetches all credit cards stored for a given user ID.
     *
     * @param userId The user's unique identifier.
     * @return List of CreditCard objects or error message.
     * @throws CreditCardException if validation or retrieval fails.
     */
	@GetMapping("/getCardDetails/{userId}")
	public ResponseEntity<?> getCardDetails(@PathVariable int userId) throws CreditCardException{
		List<CreditCard> cardDetails = new ArrayList<>();
		try {
			String error = ControllerHelper.validate(userId, CreditCardControllerConstants.USER_ID);
			if(error != null) {				
	            return ResponseEntity.badRequest().body("Please input the user Id for which you would like to retrieve the Credit cards information");
			}
			List<String> akvSecrets = CreditCardDao.getActiveAkvSecretsByUserId(userId);
			
			cardDetails = CreditCardAkvSecretHandler.getCardDetailsByUser(AkvConstants.akvName, akvSecrets, userId);			
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("Failed to get the Card details");
		}
		return ResponseEntity.ok(cardDetails);
	}
	
	/**
     * Validates the card details provided by user.
     *
     * Performs:
     * - Null checks
     * - Format validation
     * - Expiry date validation
     * - Luhn checksum validation
     * - Blacklist check
     *
     * @param creditcard Card data to validate.
     * @return ResponseEntity with success or validation error.
     * @throws Exception if any validation or parsing fails.
     */
	@PostMapping("/validateCardDetails")
	public ResponseEntity<Map<String, Object>> validateCardDetails(@RequestBody(required = false) CreditCard creditcard) throws Exception {
	    Map<String, Object> response = new HashMap<>();
	    if (creditcard == null) {
            response.put("error", "Card object is null. Please provide card details in the request body.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
	    
		String cardNumber = creditcard.getCardNumber();
		String expiryDate = creditcard.getExpiryDate();
		
		String error = ControllerHelper.validate(cardNumber, CreditCardControllerConstants.CARD_NUMBER);
		if(error != null) {
			response.put("error", error);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		error = ControllerHelper.validate(expiryDate, CreditCardControllerConstants.EXPIRY_DATE);
		if(error != null) {
			response.put("error", error);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		cardNumber = cardNumber.replaceAll("\\s", "");
		if(!CardValidationHelper.isVisaOrMasterCard(cardNumber)) {
			response.put("error", "Invalid credit card, Only Visa and Mastercard are supported");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		if(!CardValidationHelper.isValidCardLength(cardNumber)) {
			response.put("error", "Invalid card number length for Visa and MasterCard");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		if(!CardValidationHelper.isExpiryValid(expiryDate)) {
			response.put("error", "Card is expired");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		if(CardValidationHelper.isBlacklisted(cardNumber)) {
			response.put("error", "Card is blacklisted");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		if(!CardValidationHelper.isValidCardNumber(cardNumber)) {
			response.put("error", "Invalid card number (Luhn check failed)");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		response.put("message", "Card validated successfully");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}