package com.creditcard.model;

import java.util.Date;

/**
 * Represents a user in the Credit Card Management System.
 * 
 * This class contains user-related information including their name, email and timestamps for record creation and modification.
 */
public class User {
	private int userId;
	private String userName;
	private String emailAddress;
	private Date createdOn;
	private Date modifiedOn;
	
	public User() {		
	}
	
	public User(String name, String email) {
		this.userName = name;
		this.emailAddress = email;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}
}
