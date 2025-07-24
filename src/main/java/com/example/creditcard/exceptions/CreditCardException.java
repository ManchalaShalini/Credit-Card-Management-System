package com.example.creditcard.exceptions;

public class CreditCardException extends Exception{

	public CreditCardException(String message) {
		super(message);
	}
	
	public CreditCardException(String message, Exception ex) {
		super(message, ex);
	}
}
