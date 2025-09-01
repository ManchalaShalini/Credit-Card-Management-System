package com.creditcard.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.creditcard.akv.CreditCardAkvSecretHandler;
import com.creditcard.constants.AkvConstants;
import com.creditcard.constants.CreditCardConstants;
import com.creditcard.constants.DatabaseConstants;
import com.creditcard.dao.CreditCardDao;
import com.creditcard.model.CreditCard;
import com.creditcard.model.ValidationStatus;
import com.creditcard.model.CreditCardVault;
import com.creditcard.utils.AkvSecretHelper;
import com.creditcard.utils.CardValidationHelper;
import com.creditcard.utils.ControllerHelper;


/**
 * REST Controller for managing credit card information.
 * 
 * This controller provides endpoints to:
 * - Save card details
 * - Update existing card details
 * - Delete card details
 * - Retrieve card details by user
 * - Validate card details
 * 
 * All sensitive card data is securely stored in Azure Key Vault, and metadata is maintained in the database.
 */
@RestController
@RequestMapping("/creditcard")
public class CreditCardController {
	
	 @Autowired
	 private CreditCardDao creditCardDao;
	 
	 @Autowired
	 private CreditCardAkvSecretHandler creditCardAkvSecretHandler;
	 
	 @Autowired
	 private ControllerHelper controllerHelper;
	 
	 @Autowired
	 private AkvSecretHelper akvSecretHelper;
	 
	 @Autowired
	 private CardValidationHelper cardValidationHelper;
	 	 
	 /**
	  * Stores credit card details in Azure Key Vault and metadata in DB.
	  * @param creditcard JSON payload representing the credit card data.
	  * @return ResponseEntity with success or error message.
	  */
	 @PostMapping("/saveCard")
	 public ResponseEntity<Map<String, Object>> storeCardDetails(@RequestBody CreditCard creditcard) {
		 
		Map<String, Object> response = new HashMap<>();
		try {
			ResponseEntity<Map<String, Object>> validationResponse = validateUserId(creditcard.getUserID());			
			if(validationResponse != null) return validationResponse;
			
			validationResponse = validateCardNumber(creditcard.getCardNumber());			
			if(validationResponse != null) return validationResponse;			
			
			validationResponse = validateExpiryDate(creditcard.getExpiryDate());			
			if(validationResponse != null) return validationResponse;			
							
			String secretName = akvSecretHelper.generateSecretName();

			creditCardDao.storeCardAndSecretMetadata(creditcard.getUserID(), secretName);
			
			creditCardAkvSecretHandler.storeCard(creditcard, AkvConstants.akvName, secretName);
			
		    response.put("message", "Card details stored successfully");
		    return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (Exception e) {
			response.put("error", "Failed to store card details: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	 /**
	     * Updates existing card details for a given user.
	     *
	     * @param creditcard Updated card information.
	     * @return ResponseEntity indicating result.
	     */
	@PutMapping("/updateCard")
	public ResponseEntity<Map<String, Object>> updateCardDetails(@RequestBody CreditCard creditcard) {
	    Map<String, Object> response = new HashMap<>(); 
		try {		
			ResponseEntity<Map<String, Object>> validationResponse = validateUserId(creditcard.getUserID());			
			if(validationResponse != null) return validationResponse;
			
			validationResponse = validateCardNumber(creditcard.getCardNumber());			
			if(validationResponse != null) return validationResponse;			
			
			validationResponse = validateExpiryDate(creditcard.getExpiryDate());			
			if(validationResponse != null) return validationResponse;								
			
			List<String> akvSecrets = creditCardDao.getAkvSecretsByUserId(creditcard.getUserID(), DatabaseConstants.ACTIVE);
			
			if (akvSecrets == null || akvSecrets.isEmpty()) {
				response.put("error", "Card information does not exists for this user");
		        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
			}
			
			creditCardAkvSecretHandler.updateCard(creditcard, AkvConstants.akvName, akvSecrets);
			response.put("message", "Card details updated successfully");
	        return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			response.put("error", "Failed to update Card details: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
     * Deletes card details from Azure Key Vault and marks it inactive in DB.
     *
     * @param creditcard Card data used to locate and delete the record.
     * @return ResponseEntity with result message.
     */
	@DeleteMapping("/deleteCard")
	public ResponseEntity<Map<String, Object>> deleteCardDetails(@RequestBody CreditCard creditcard) {
		Map<String, Object> response = new HashMap<>();
		try {
			ResponseEntity<Map<String, Object>> validationResponse = validateUserId(creditcard.getUserID());			
			if(validationResponse != null) return validationResponse;
			
			validationResponse = validateCardNumber(creditcard.getCardNumber());			
			if(validationResponse != null) return validationResponse;						
			
			List<String> akvSecrets = creditCardDao.getAkvSecretsByUserId(creditcard.getUserID(), DatabaseConstants.ACTIVE);			
			
			if (akvSecrets == null || akvSecrets.isEmpty()) {
				response.put("error", "Card information does not exists for this user");
		        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
			}
			
			// TODO: in the end - need to see if we can give id to user instead of fetching all cards and
			// checking in akv which matched
			String akvSecretName = creditCardAkvSecretHandler.deleteCard(AkvConstants.akvName, akvSecrets, creditcard.getCardNumber());
			if(akvSecretName == null) {
				response.put("message", "Card details does not exist in our system");
		        return new ResponseEntity<>(response, HttpStatus.OK);
			}
			
			creditCardDao.updateCardAndSecretMetadata(creditcard.getUserID(), akvSecretName, DatabaseConstants.INACTIVE);
						
			response.put("message", "Card details deleted successfully");
	        return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.put("error", "Failed to delete Card details: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
		
	 /**
     * Fetches all credit cards stored for a given user ID.
     * @param userId The user's unique identifier.
     * @return List of CreditCard objects or error message.
     */
	@GetMapping("/getCard/{userId}")
	public ResponseEntity<List<CreditCardVault>> getCardDetails(@PathVariable int userId) {
		List<CreditCardVault> cardDetails = new ArrayList<>();
		try {												
			List<String> akvSecrets = creditCardDao.getAkvSecretsByUserId(userId, DatabaseConstants.ACTIVE);
			
			cardDetails = creditCardAkvSecretHandler.getCardsByUser(AkvConstants.akvName, akvSecrets, userId);	
			
			return ResponseEntity.ok(cardDetails);
		} catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(cardDetails);
		}
	}
	
	private ResponseEntity<Map<String, Object>> validateUserId(int userID) {
		ValidationStatus status = controllerHelper.validate(userID, CreditCardConstants.USER_ID);
		if(!status.isValid()) {
	        Map<String, Object> response = new HashMap<>();
			response.put("error", status.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return null;
	}
	
	private ResponseEntity<Map<String, Object>> validateCardNumber(String cardNumber) {
		ValidationStatus status = controllerHelper.validate(cardNumber, CreditCardConstants.CARD_NUMBER);
		if(!status.isValid()) {
	        Map<String, Object> response = new HashMap<>();
			response.put("error", status.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return null;
	}
	
	private ResponseEntity<Map<String, Object>> validateExpiryDate(String expiryDate) {
		ValidationStatus status = controllerHelper.validate(expiryDate, CreditCardConstants.EXPIRY_DATE);
		if(!status.isValid()) {
	        Map<String, Object> response = new HashMap<>();
			response.put("error", status.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return null;
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
     */
	@PostMapping("/validateCard")
	public ResponseEntity<Map<String, Object>> validateCardDetails(@RequestBody CreditCard creditcard) {
	    Map<String, Object> response = new HashMap<>();
	    try {
			String cardNumber = creditcard.getCardNumber();
			String expiryDate = creditcard.getExpiryDate();
			
			ResponseEntity<Map<String, Object>> validationResponse = validateCardNumber(creditcard.getCardNumber());			
			if(validationResponse != null) return validationResponse;			
			
			validationResponse = validateExpiryDate(creditcard.getExpiryDate());			
			if(validationResponse != null) return validationResponse;

			// "\s" is whitespace character. In java \\ actually means \ , because the backslash is an escape character and needs to be 
			// escaped to be a backslash in a string.
			cardNumber = cardNumber.replaceAll("\\s", "");  
															  
			if(!cardValidationHelper.isValidCardLength(cardNumber)) {
				response.put("error", "Invalid card number length for Visa or MasterCard");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			if(!cardValidationHelper.isValidCardNumber(cardNumber)) {
				response.put("error", "Invalid card number (Luhn check failed)");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(!cardValidationHelper.isVisaOrMasterCard(cardNumber)) {
				response.put("error", "Invalid credit card, only Visa or Mastercard are supported currently");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
								
			if(!cardValidationHelper.isExpired(expiryDate)) {
				response.put("error", "Card is expired");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(cardValidationHelper.isBlacklisted(cardNumber)) {
				response.put("error", "Card is blacklisted");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			response.put("message", "Card validated successfully");
			return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception ex) {
	    	response.put("message", "Exception while validating credit card");
	    	return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	 }
}