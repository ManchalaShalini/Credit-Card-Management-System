package com.example.creditcard.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.example.creditcard.exceptions.CreditCardException;

/**
 * Helper utility class for validating credit card details such as card number,
 * expiry date, and card type.
 */
public class CardValidationHelper {
	
	 /**
     * Predefined blacklist of dummy/test card numbers that should be rejected during validation.
     */
	 private static final Set<String> BLACKLISTED_CARDS = new HashSet<>(Arrays.asList(
		        "4111111111111111", "5500000000000004"
		    ));


	/**
	 * Checks if the card number passes the Luhn algorithm.
	 *
	 * @param cardNumber Card number to validate.
	 * @return true if valid.
	 */
	public static boolean isValidCardNumber(String cardNumber) {
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

	/**
	 * Checks if the card number exists in the blacklist.
	 *
	 * @param cardNumber Card number to check.
	 * @return true if blacklisted.
	 */
	public static boolean isBlacklisted(String cardNumber) {		
		return BLACKLISTED_CARDS.contains(cardNumber);
	}

	/**
	 * Validates that the expiry date is in MM/YY format and not expired.
	 *
	 * @param expiryDate Expiry date string.
	 * @return true if valid.
	 * @throws CreditCardException if date format is wrong or expired.
	 */
	public static boolean isExpiryValid(String expiryDate) throws CreditCardException {
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

	/**
	 * Validates length based on card type (Visa/MasterCard).
	 *
	 * @param cardNumber Card number to check.
	 * @return true if length is valid.
	 * @throws CreditCardException if card length doesn't match type.
	 */
	public static boolean isValidCardLength(String cardNumber) throws CreditCardException {
		if (cardNumber.startsWith("4")) {
            return cardNumber.length() == 13 || cardNumber.length() == 16 || cardNumber.length() == 19;
        } else if (cardNumber.matches("^(5[1-5]|2(2[2-9]|[3-6][0-9]|7[01]|720)).*")) {
            return cardNumber.length() == 16;
        }
        return false;
	}

	/**
	 * Checks if the card is either Visa or MasterCard Type.
	 *
	 * @param cardNumber Card number.
	 * @return true if card is Visa or MasterCard.
	 */
	public static boolean isVisaOrMasterCard(String cardNumber) {
		return cardNumber.startsWith("4") || // Visa
	           (cardNumber.startsWith("51") || cardNumber.startsWith("52") ||
	            cardNumber.startsWith("53") || cardNumber.startsWith("54") ||
	            cardNumber.startsWith("55") || // MasterCard (old range)
	            cardNumber.matches("^2(2[2-9]|[3-6][0-9]|7[01]|720).*")); // MasterCard (new range)
	}
}
