package com.creditcard.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creditcard.constants.DatabaseConstants;
import com.creditcard.constants.UserConstants;
import com.creditcard.dao.CreditCardDao;
import com.creditcard.dao.UserDao;
import com.creditcard.exceptions.UserException;
import com.creditcard.model.User;
import com.creditcard.model.ValidationStatus;
import com.creditcard.utils.ControllerHelper;

/**
 * REST controller responsible for managing user-related operations.
 * 
 * Exposes APIs to create, retrieve, update, and delete user records.
 * Validates incoming request data and communicates with the data access layer (UserDao) to perform operations.
 */
@RestController
@RequestMapping("/user")
public class UserController {
	 @Autowired
     private UserDao userDao;
	 
	 @Autowired
     private CreditCardDao creditCardDao;
	 
	 @Autowired
	 private ControllerHelper controllerHelper;
	     
    /**
     * API to create a new user.
     *
     * @param user The user object passed in the request body.
     * @return A ResponseEntity with success message and created user, or error if validation fails or user is null.
     */
	@PostMapping("/createUser")
	public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) { 
	    Map<String, Object> response = new HashMap<>();
		try {			
			ResponseEntity<Map<String, Object>> validationResponse = validateUserName(user.getUserName());
	        if(validationResponse != null) return validationResponse;
	        
	        validationResponse = validateEmailAddress(user.getEmailAddress());
	        if(validationResponse != null) return validationResponse;													
						
			user = userDao.createUser(user);			
		    response.put("message", "User created successfully");
		    response.put("user", user);
		    return new ResponseEntity<>(response, HttpStatus.CREATED);   
		} catch (Exception e) {
			response.put("error", "Failed to create User: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	 }
	
	 /**
     * API to retrieve a user by ID.
     *
     * @param userId The ID of the user to fetch.
     * @return A ResponseEntity with user details if found, or appropriate error message.
     */
	@GetMapping("/getUser/{userId}")
	public ResponseEntity<Map<String, Object>> getUser(@PathVariable int userId) {
	    Map<String, Object> response = new HashMap<>();
		User user = null;
		try {
			user = userDao.getUser(userId);
			if(user == null) {
				response.put("error", "User not found");
		        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			
		    response.put("message", "User details retrieved successfully");
		    response.put("user", user);	
		    return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.put("error", "Failed to get User details: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}
	
	/**
     * API to delete a user by user ID.
     *
     * @param userId The ID of the user to delete.
     * @return A ResponseEntity indicating success or failure of deletion.
     */
	@DeleteMapping("/deleteUser/{userId}")
	public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable int userId) {
	    Map<String, Object> response = new HashMap<>();
		try {					
			// Validation to check if user exists in the system
			ResponseEntity<Map<String, Object>> userValidation = getUser(userId);
	        if (userValidation.getStatusCode() != HttpStatus.OK) {
	            return userValidation;
	        }
	        
	        // Validation to check if there are any active cards related to user
	        List<String> akvSecretsList = creditCardDao.getAkvSecretsByUserId(userId, DatabaseConstants.ACTIVE);
	        if(akvSecretsList != null  && !akvSecretsList.isEmpty()) {
	        	response.put("error", "Cannot delete user. Delete all active cards linked to this user prior to deleting the user.");
		        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
	        }

	        boolean deleted = userDao.deleteUser(userId);
			if(!deleted) {
				throw new UserException("Exception while deleting User record");
			}
			
	        response.put("message", "User deleted successfully");
	        return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			response.put("error", "Failed to delete User: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
     * API to update an existing user's name and email.
     *
     * @param userId The ID of the user to update.
     * @param updatedUser The user object with updated fields.
     * @return A ResponseEntity indicating whether the update was successful.
     */
	@PutMapping("/updateUser/{userId}")
	public ResponseEntity<Map<String, Object>> updateUser(@PathVariable int userId, @RequestBody User user) {
	    Map<String, Object> response = new HashMap<>();
		try {
			ResponseEntity<Map<String, Object>> validationResponse = validateUserName(user.getUserName());
	        if(validationResponse != null) return validationResponse;
	        
	        validationResponse = validateEmailAddress(user.getEmailAddress());
	        if(validationResponse != null) return validationResponse;
	        
			ResponseEntity<Map<String, Object>> userValidation = getUser(userId);
	        if (userValidation.getStatusCode() != HttpStatus.OK) {
	            return userValidation;
	        }				        												
			
			boolean updated = userDao.updateUser(userId, user.getUserName(), user.getEmailAddress());
			if(!updated) {
				throw new UserException("Exception while updating user record");
			}
			
			response.put("message", "User details updated successfully");
	        return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			response.put("error", "Failed to update user details: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	private ResponseEntity<Map<String, Object>> validateUserName(String name) {
		ValidationStatus status = controllerHelper.validate(name, UserConstants.NAME);
		if(!status.isValid()) {
	        Map<String, Object> response = new HashMap<>();
			response.put("error", status.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return null;
	}
	
	private ResponseEntity<Map<String, Object>> validateEmailAddress(String email) {
		ValidationStatus status = controllerHelper.validate(email, UserConstants.EMAIL);
		if(!status.isValid()) {
	        Map<String, Object> response = new HashMap<>();
			response.put("error", status.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return null;
	}
}