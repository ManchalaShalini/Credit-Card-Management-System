package com.example.creditcard.utils;

/**
 * Utility helper class for validating various controller input fields.
 */
public class ControllerHelper {
	 /**
     * Validates a String field value based on whether it's null, empty,
     * and performs additional validation for email format if the field is an email.
     *
     * @param fieldValue The String value to validate.
     * @param fieldName  The name of the field (used for error messages, e.g., "Email").
     * @return An error message if validation fails, or null if validation passes.
     */
	public static String validate(String fieldValue, String fieldName) {
		if(fieldValue == null || fieldValue.trim().isEmpty()) {
            return fieldName + " is required";
		}
		
        // Email format validation
		if("Email".equals(fieldName)) {
			String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
		    if(!fieldValue.matches(emailRegex)) {
		    	return "Invalid Email Format";
		    }
		}
		return null;
	}
	
	/**
     * Validates an integer field value to check if it is zero.
     *
     * @param fieldValue The int value to validate.
     * @param fieldName  The name of the field (used for error messages).
     * @return An error message if the value is zero, or null if validation passes.
     */
	public static String validate(int fieldValue, String fieldName) {
		if(fieldValue == 0) {
			return "Provide correct "+ fieldName + " details";
		}
		return null;
	}
}
