package com.creditcard.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.creditcard.exceptions.CreditCardException;

/**
 * Helper utility class for validating credit card details such as card number (luhn check), expiry date, card type, card number length and 
 * blacklisted check.
 */
@Component
public class CardValidationHelper {
	
	 /**
     * Predefined blacklist of dummy/test card numbers that should be rejected during validation.
     */
	 private final Set<String> BLACKLISTED_CARDS = new HashSet<>(Arrays.asList(
		        "4111111111111111", "5500000000000004"
		    ));


	/**
	 * Checks if the card number passes the Luhn algorithm.
	 *
	 * @param cardNumber Card number to validate.
	 * @return true if valid.
	 */
	public boolean isValidCardNumber(String cardNumber) {
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
	public boolean isBlacklisted(String cardNumber) {		
		return BLACKLISTED_CARDS.contains(cardNumber);
	}

	/**
	 * Validates that the expiry date is in MM/YY format and not expired.
	 *
	 * @param expiryDate Expiry date string.
	 * @return true if valid.
	 * @throws CreditCardException if date format is wrong or expired.
	 */
	public boolean isExpired(String expiryDate) throws CreditCardException {
		if (!Pattern.matches("(0[1-9]|1[012])/\\d{2}", expiryDate)) {
            throw new CreditCardException("Expiry Date is in a wrong format, please provide the details in MM/YY format");
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
	 * Validates card number length.
	 *
	 * @param cardNumber Card number to check.
	 * @return true if length is valid.
	 */
	public boolean isValidCardLength(String cardNumber) {
		// Visa supports cards of length 13, 16, 19 and Master card supports cards of length 16
        return cardNumber.length() == 13 || cardNumber.length() == 16 || cardNumber.length() == 19;
	}

	/**
	 * Checks if the card is either Visa or MasterCard Type.
	 * @param cardNumber Card number.
	 * @return true if card is Visa or MasterCard.
	 */
	public boolean isVisaOrMasterCard(String cardNumber) {
		return isVisaCard(cardNumber) || isMasterCard(cardNumber);
	}

	private boolean isMasterCard(String cardNumber) {
		return cardNumber.startsWith("4");
	}

	private boolean isVisaCard(String cardNumber) {
		return (cardNumber.matches("^5[1-5].*") || // MasterCard (old range)
	            cardNumber.matches("^2(2[2-9]|[3-6][0-9]|7[01]|720).*")); // MasterCard (new range)
	}
}
