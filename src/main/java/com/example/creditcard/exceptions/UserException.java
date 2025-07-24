package com.example.creditcard.exceptions;

public class UserException extends Exception{

	public UserException(String message) {
		super(message);
	}
	
	public UserException(String message, Exception ex) {
		super(message, ex);
	}
}
