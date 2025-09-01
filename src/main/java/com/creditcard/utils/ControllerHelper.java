package com.creditcard.utils;

import org.springframework.stereotype.Component;

import com.creditcard.model.ValidationStatus;


/**
 * Utility helper class for validating various controller input fields.
 */
@Component
public class ControllerHelper {
	 /**
     * Validates a String field value based on whether it's null, empty,
     * and performs additional validation for email format if the field is an email.
     *
     * @param fieldValue The String value to validate.
     * @param fieldName  The name of the field (used for error messages, e.g., "Email").
     * @return An error message if validation fails, or null if validation passes.
     */
	public ValidationStatus validate(String fieldValue, String fieldName) {
		if(fieldValue == null || fieldValue.trim().isEmpty()) {
            return new ValidationStatus(false, fieldName + " cannot be null or empty");
		}
		
        // Email format validation
		if("Email".equals(fieldName)) {
			String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
		    if(!fieldValue.matches(emailRegex)) {
		    	return new ValidationStatus(false, "Invalid Email Format");
		    }
		}
        return new ValidationStatus(true, fieldName + " is valid");
	}
	
	/**
     * Validates an integer field value to check if it is zero.
     *
     * @param fieldValue The int value to validate.
     * @param fieldName  The name of the field (used for error messages).
     * @return An error message if the value is zero, or null if validation passes.
     */
	public ValidationStatus validate(int fieldValue, String fieldName) {
		if(fieldValue == 0) {
            return new ValidationStatus(false, fieldName + " cannot be zero");
		}
        return new ValidationStatus(true, fieldName + " is valid");
     }
}
