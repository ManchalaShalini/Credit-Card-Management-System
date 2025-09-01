package com.creditcard.model;

/**
 * Represents a credit card associated with a user. This class holds card metadata such as the card number and expiry date to be 
 * stored in the Key vault.
 */
public class CreditCardVault {
	private String cardNumber;
	private String expiryDate;
	
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}
}
