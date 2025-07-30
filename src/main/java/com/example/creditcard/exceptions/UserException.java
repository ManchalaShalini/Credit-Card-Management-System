package com.example.creditcard.exceptions;

/**
 * Custom exception class for handling errors related to User operations.
 * This exception is typically thrown when something goes wrong while processing
 * user metadata, secret storage, or retrieval logic.
 */
public class UserException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Constructs a new UserException with the specified detail message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
	public UserException(String message) {
		super(message);
	}
	
	/**
     * Constructs a new UserException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param ex The original exception that caused this exception to be thrown.
     */
	public UserException(String message, Exception ex) {
		super(message, ex);
	}
}
