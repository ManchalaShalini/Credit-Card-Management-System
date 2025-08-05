package com.example.creditcard.controller;

import java.util.HashMap;
import java.util.Map;

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

import com.example.creditcard.constants.UserControllerConstants;
import com.example.creditcard.dao.UserDao;
import com.example.creditcard.exceptions.UserException;
import com.example.creditcard.model.User;
import com.example.creditcard.utils.ControllerHelper;

/**
 * REST controller responsible for managing user-related operations.
 * 
 * Exposes APIs to create, retrieve, update, and delete user records.
 * Validates incoming request data and communicates with the data access layer (`UserDao`) to perform operations.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private static UserDao userDao;

    UserController(UserDao userDao) {
        this.userDao = userDao;
    }
    
    /**
     * API to create a new user.
     *
     * @param user The user object passed in the request body.
     * @return A ResponseEntity with success message and created user, or error if validation fails or user is null.
     */
	@PostMapping("/createUser")
	public ResponseEntity<Map<String, Object>> createUser(@RequestBody(required = false) User user) { 
		//@RequestBody(required = false) allows Spring to pass null instead of throwing a 400 Bad Request automatically when the body is missing — this way you can return a custom message.

	    Map<String, Object> response = new HashMap<>();
		try {
			if (user == null) {
	            response.put("error", "User object is null. Please provide user details in the request body.");
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	        }
			
			String error = ControllerHelper.validate(user.getUserName(), UserControllerConstants.NAME);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			error = ControllerHelper.validate(user.getEmailAddress(), UserControllerConstants.EMAIL);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}			
						
			user = userDao.createUser(user);			
		    response.put("message", "User created successfully");
		    response.put("user", user);
		    
		} catch (Exception e) {
			response.put("error", "Failed to create User: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    return new ResponseEntity<>(response, HttpStatus.CREATED);
	 }
	
	 /**
     * API to retrieve a user by user ID.
     *
     * @param userId The ID of the user to fetch.
     * @return A ResponseEntity with user details if found, or appropriate error message.
     */
	@GetMapping("/getUser/{userId}")
	public ResponseEntity<Map<String, Object>> getUser(@PathVariable int userId) {
	    Map<String, Object> response = new HashMap<>();
		User user = null;
		try {
			String error = ControllerHelper.validate(userId, UserControllerConstants.USER_ID);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			user = userDao.getUser(userId);
			if(user == null) {
				response.put("error", "User not found");
		        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			
		    response.put("message", "User details retrieved successfully");
		    response.put("user", user);		    
		} catch (Exception e) {
			response.put("error", "Failed to get User details: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	    return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
     * API to delete a user by user ID.
     *
     * @param userId The ID of the user to delete.
     * @return A ResponseEntity indicating success or failure of deletion.
     * @throws UserException If deletion fails at the DAO layer.
     */
	@DeleteMapping("/deleteUser/{userId}")
	public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable int userId) throws UserException {
	    Map<String, Object> response = new HashMap<>();
		try {					
			ResponseEntity<Map<String, Object>> getUserResponse = getUser(userId);
	        if (getUserResponse.getStatusCode() != HttpStatus.OK) {
	            return getUserResponse;
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
	public ResponseEntity<Map<String, Object>> updateUser(@PathVariable int userId, @RequestBody(required = false) User updatedUser) {
	    Map<String, Object> response = new HashMap<>();
		try {
			if (updatedUser == null) {
	            response.put("error", "User object is null. Please provide user details in the request body.");
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	        } 
			
			ResponseEntity<Map<String, Object>> getUserResponse = getUser(userId);
	        if (getUserResponse.getStatusCode() != HttpStatus.OK) {
	            return getUserResponse;
	        }
			
	        String error = ControllerHelper.validate(updatedUser.getUserName(), UserControllerConstants.NAME);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			error = ControllerHelper.validate(updatedUser.getEmailAddress(), UserControllerConstants.EMAIL);
			if(error != null) {
				response.put("error", error);
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}								
			
			boolean updated = userDao.updateUser(userId, updatedUser.getUserName(), updatedUser.getEmailAddress());
			if(!updated) {
				throw new UserException("Exception while updating User record");
			}
			
			response.put("message", "User details updated successfully");
	        return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			response.put("error", "Failed to update User: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
}