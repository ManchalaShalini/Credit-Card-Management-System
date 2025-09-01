package com.creditcard.model;

/**
 * Represents the result of validating user input.
 * 
 * This class is typically used to indicate whether a user-provided input passes validation checks. 
 * If validation fails, it also provides a descriptive message about the failure. 
 * */
public class ValidationStatus {
	private boolean isValid;
	private String message;
	
	public ValidationStatus(boolean isValid , String msg) {
		this.setValid(isValid);
		this.setMessage(msg);
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
