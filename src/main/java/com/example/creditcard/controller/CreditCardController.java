package com.example.creditcard.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.example.creditcard.utils.ControllerHelper;

@RestController
@RequestMapping("/api")
public class CreditCardController {
	
	// Blacklisted the following dummy or test card numbers to perform blacklist validation checks on credit card input.
	 private static final Set<String> BLACKLISTED_CARDS = new HashSet<>(Arrays.asList(
		        "4111111111111111", "5500000000000004"
		    ));
	 
	 @GetMapping("/storeCardDetails")
	 public static ResponseEntity<Map<String, Object>> storeCardDetails(@RequestBody(required = false) CreditCard cardDetails) throws CreditCardException {
		 
		Map<String, Object> response = new HashMap<>();
		try {			
			if (cardDetails == null) {
	            response.put("error", "User object is null. Please provide user details in the request body.");
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	        } 
			
			String error = ControllerHelper.validate(cardDetails.getUserID(), CreditCardControllerConstants.userId);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			error = ControllerHelper.validate(cardDetails.getCardNumber(), CreditCardControllerConstants.cardNumber);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			error = ControllerHelper.validate(cardDetails.getExpiryDate(), CreditCardControllerConstants.expiryDate);
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
	
	@GetMapping("/updateCardDetails")
	public static ResponseEntity<Map<String, Object>> updateCardDetails(@RequestBody(required = false) CreditCard cardDetails) throws CreditCardException {
	    Map<String, Object> response = new HashMap<>();
		try {
			if (cardDetails == null) {
	            response.put("error", "Card object is null. Please provide card details in the request body.");
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	        } 
			
			String error = ControllerHelper.validate(cardDetails.getUserID(), CreditCardControllerConstants.userId);			
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			error = ControllerHelper.validate(cardDetails.getCardNumber(), CreditCardControllerConstants.cardNumber);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			error = ControllerHelper.validate(cardDetails.getExpiryDate(), CreditCardControllerConstants.expiryDate);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			List<String> akvSecrets = CreditCardDao.getAkvSecretsByUserId(cardDetails.getUserID());
			
			CreditCardAkvSecretHandler.updateCardDetails(cardDetails, AkvConstants.akvName, akvSecrets);
			response.put("message", "User details updated successfully");
	        return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			response.put("error", "Failed to update Card details: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/deleteCardDetails")
	public static ResponseEntity<Map<String, Object>> deleteCardDetails(@RequestBody(required = false) CreditCard cardDetails) throws CreditCardException {
		Map<String, Object> response = new HashMap<>();
		try {
			if (cardDetails == null) {
	            response.put("error", "Card object is null. Please provide card details in the request body.");
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	        } 
			
			String error = ControllerHelper.validate(cardDetails.getUserID(), CreditCardControllerConstants.userId);			
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			error = ControllerHelper.validate(cardDetails.getCardNumber(), CreditCardControllerConstants.cardNumber);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}					
			
			CreditCardDao.updateCardAndSecretMetadataToInactive(cardDetails.getUserID());
			
			List<String> akvSecrets = CreditCardDao.getAkvSecretsByUserId(cardDetails.getUserID());			
			// TODO: delete get secret id from d and use that to delete
			CreditCardAkvSecretHandler.deleteCardDetails(AkvConstants.akvName, akvSecrets, cardDetails.getCardNumber());
			
			response.put("message", "User deleted successfully");
	        return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.put("error", "Failed to delete User: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/getCardDetails")
	public static ResponseEntity<?> getCardDetails(@PathVariable int userId) throws CreditCardException{
		List<String> cardDetails = new ArrayList<>();
		try {
			String error = ControllerHelper.validate(userId, CreditCardControllerConstants.userId);
			if(error != null) {				
	            return ResponseEntity.badRequest().body("Please input the user Id for which you would like to retrieve the Credit cards information");
			}
			List<String> akvSecrets = CreditCardDao.getAkvSecretsByUserId(userId);
			
			cardDetails = CreditCardAkvSecretHandler.getCardDetailsByUser(AkvConstants.akvName, akvSecrets);			
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("Failed to get the Card details");
		}
		return ResponseEntity.ok(cardDetails);
	}
	
	@PostMapping("/validateCardDetails")
	public static ResponseEntity<Map<String, Object>> validateCardDetails(@RequestBody(required = false) CreditCard creditcard) throws Exception {
	    Map<String, Object> response = new HashMap<>();
	    if (creditcard == null) {
            response.put("error", "Card object is null. Please provide card details in the request body.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
	    
		String cardNumber = creditcard.getCardNumber();
		String expiryDate = creditcard.getExpiryDate();
		
		String error = ControllerHelper.validate(cardNumber, CreditCardControllerConstants.cardNumber);
		if(error != null) {
			response.put("error", error);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		error = ControllerHelper.validate(expiryDate, CreditCardControllerConstants.expiryDate);
		if(error != null) {
			response.put("error", error);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		if(!isVisaOrMasterCard(cardNumber)) {
			response.put("error", "Invalid credit card, Only Visa and Mastercard are supported");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		if(!isValidCardLength(cardNumber)) {
			response.put("error", "Invalid card number length for Visa and MasterCard");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		if(!isExpiryValid(expiryDate)) {
			response.put("error", "Card is expired");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		if(isBlacklisted(cardNumber)) {
			response.put("error", "Card is blacklisted");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		cardNumber = cardNumber.replaceAll("\\s", "");
		if(!isValidCardNumber(cardNumber)) {
			response.put("error", "Invalid card number (Luhn check failed)");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		response.put("message", "Card validated successfully");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	private static boolean isValidCardNumber(String cardNumber) {
		int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
	}

	private static boolean isBlacklisted(String cardNumber) {		
		return BLACKLISTED_CARDS.contains(cardNumber);
	}

	private static boolean isExpiryValid(String expiryDate) throws CreditCardException {
		if (!Pattern.matches("(0[1-9]|1[012])/\\d{2}", expiryDate)) {
            throw new CreditCardException("Expiry Date is in a wrolng format, please provide the details in MM/YY format");
        }
		String pattern = "MM/yy";
        Date expDate = null;
        try {
            expDate = new SimpleDateFormat(pattern).parse(expiryDate);
            return new Date().before(expDate);
        } catch (Exception e) {
            throw new CreditCardException("Error while parsing Date", e);
        }
	}

	private static boolean isValidCardLength(String cardNumber) throws CreditCardException {
		if (cardNumber.startsWith("4")) {
            return cardNumber.length() == 13 || cardNumber.length() == 16 || cardNumber.length() == 19;
        } else if (cardNumber.matches("^(5[1-5]|2(2[2-9]|[3-6][0-9]|7[01]|720)).*")) {
            return cardNumber.length() == 16;
        }
        return false;
	}

	private static boolean isVisaOrMasterCard(String cardNumber) {
		return cardNumber.startsWith("4") || // Visa
	           (cardNumber.startsWith("51") || cardNumber.startsWith("52") ||
	            cardNumber.startsWith("53") || cardNumber.startsWith("54") ||
	            cardNumber.startsWith("55") || // MasterCard (old range)
	            cardNumber.matches("^2(2[2-9]|[3-6][0-9]|7[01]|720).*")); // MasterCard (new range)
	}
}
