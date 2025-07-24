package com.example.creditcard.utils;

public class ControllerHelper {
	public static String validate(String fieldValue, String fieldName) {
		if(fieldValue == null || fieldValue.trim().isEmpty()) {
            return fieldName + " is required";
		}
		
		if(fieldName == "Email") {
			String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
		    if(!fieldValue.matches(emailRegex)) {
		    	return "Invalid Email Format";
		    }
		}
		return null;
	}
	
	public static String validate(int fieldValue, String fieldName) {
		if(fieldValue == 0) {
			return "Provide correct "+ fieldName + " details";
		}
		return null;
	}
}
