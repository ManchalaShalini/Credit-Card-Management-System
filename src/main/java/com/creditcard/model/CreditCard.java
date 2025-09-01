package com.creditcard.model;

/**
 * Represents a credit card associated with a user.
 * 
 * This class holds card metadata such as the card number and expiry date and other identifiers.
 */
public class CreditCard {
	//@JsonIgnore
	//private int cardId;
	private int userID;
	private String cardNumber;
	private String expiryDate;
	
	public CreditCard() {		
	}
	
	public CreditCard(String cardNumber, String expDate, int user) {
		this.cardNumber = cardNumber;
		this.expiryDate = expDate;
		this.userID = user;
	}	
	
	public CreditCard(String cardNumber, String expDate) {
		this.cardNumber = cardNumber;
		this.expiryDate = expDate;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

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
