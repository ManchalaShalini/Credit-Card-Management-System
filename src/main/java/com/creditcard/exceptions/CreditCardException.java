package com.creditcard.exceptions;

/**
 * Custom exception class for handling errors related to Credit Card operations.
 * This exception is typically thrown when something goes wrong while processing card metadata, secret storage, or retrieval logic.
 */
public class CreditCardException extends Exception{

	/**
     * Constructs a new CreditCardException with the specified detail message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
	public CreditCardException(String message) {
		super(message);
	}
	
	/**
     * Constructs a new CreditCardException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param ex The original exception that caused this exception to be thrown.
     */
	public CreditCardException(String message, Exception ex) {
		super(message, ex);
	}
}
