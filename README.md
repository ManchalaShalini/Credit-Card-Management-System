# CreditCard Management Project

#### Project Overview
The Credit Card Management System is a Spring Boot based Java application designed to securely manage user and credit card information. It integrates with Azure Key Vault for secret storage and Azure Database for PostgreSQL for persistent data management.

#### Core Features
- **User Management** — Create, updates and deletes user profiles.

- **Credit Card Management** — Store credit card details securely using Azure Key Vault, validates card details using Luhn's algorithm. Supports other CRUD operations such as update, delete and get card details.

#### Architecture Overview

![Architecture Diagram](images/ArchitectureDiagrams/Architecture_Diagram.PNG)

- **Azure Key Vault (AKV)** - Provides secure storage and management of sensitive information (i.e., credit card details).  
- **Azure Database for PostgreSQL server** - Stores metadata information for User and CreditCard.

### Key Components
- **Controllesr Layer** - Handles API requests and responses (e.g., UserController, CreditCardController).
- **Data Access Layer** - Connects to Azure Database for PostgreSQL server to store and retrieve application data.
- **CreditCardAkvSecretHandler.java** - Handler class to interact with Azure Key Vault for storing, updating, retrieving and deleting credit card information stored as secrets.

### Deployment
- The application is hosted on Azure Virtual Machine (VM).

#### Database Schema

![Database Schema Diagram](images/DatabaseSchema/Database_Schema.PNG)

#### API Design
##### User Controller API's
- POST /user/createUser -- Create User
- GET /user/getUser/{userId} -- Fetch user details for a given user
- DELETE /user/deleteUser/{userId} -- Delete user details for a given user
- PUT /user/updateUser/{userId} -- Update user details for a given user

##### Credit Card Controller API's
- POST /creditcard/saveCard -- Add credit card details
- PUT /creditcard/updateCard -- Update credit card details
- DELETE /creditcard/deleteCard -- Delete credit card details
- GET /creditcard/getCard/{userId} -- Fetch credit card details for a user
- POST /creditcard/validateCard -- Validate credit card details 

For API Request and Response, refer to section [API Request / Response](#api-request-response)

#### Sequence Diagram

###### Create User
![Create User Sequence Diagram](images/SequenceDiagrams/CreateUser.PNG)

###### Update User
![Update User Sequence Diagram](images/SequenceDiagrams/UpdateUser.PNG)

###### Delete User
![Delete User Sequence Diagram](images/SequenceDiagrams/DeleteUser.PNG)

###### Get User
![Get User Sequence Diagram](images/SequenceDiagrams/GetUser.PNG)

###### Save Card
![Save Card Sequence Diagram](images/SequenceDiagrams/SaveCard.PNG)

###### Update Card
![Update Card Sequence Diagram](images/SequenceDiagrams/UpdateCard.PNG)

###### Delete Card
![Delete Card Sequence Diagram](images/SequenceDiagrams/DeleteCard.PNG)

###### Get Card
![Get Card Sequence Diagram](images/SequenceDiagrams/GetCard.PNG)

###### Validate Card
![Validate Card Sequence Diagram](images/SequenceDiagrams/ValidateCard.PNG)


#### Getting Started

##### Prerequisites
- Java 17+
- Maven
- Azure subscription
- Azure key vault
- Azure Database for PostgreSQL server

##### Setup
   1. Clone the repository: <br>
	   git clone clone_url
   
   2. Configure AKVConstants.java and DatabaseConstants.java files with your Azure DB and Key vault details
      
   3. Navigate into the project folder: <br>
   	   cd "path-to-project-folder"
   
   4. Run below command to install pom.xml dependency jar files: <br>
   	   mvn clean install
   
   5. Run the app using the command: <br>
       mvn spring-boot:run <br>
				OR <br>
	   Right click on the project application class file - Run As - Java application
 
#### API Request / Response

###### Create User
![Create User Request and Response](images/RequestResponse/CreateUser.PNG)

###### Get User Details
![User Details Post Create](images/RequestResponse/GetUserDetails_AfterCreate.PNG)

###### Update User
![Update User Request and Response](images/RequestResponse/UpdateUser.PNG)

###### Get User Details
![User Details Post Update](images/RequestResponse/GetUserDetails_AfterUpdate.PNG)

###### Delete User
![Delete User Request and Response](images/RequestResponse/DeleteUser.PNG)

###### Get User Details
![User Details Post Delete](images/RequestResponse/GetUserDetails_AfterDelete.PNG)

###### Save Card
![Save Card Request and Response](images/RequestResponse/SaveCard.PNG)

###### Get Card Details
![Card Details Post Save](images/RequestResponse/GetCardDetails_AfterCreate.PNG)

###### Update Card (Expiry Date update)
![Update Card Request and Response](images/RequestResponse/UpdateCard.PNG)

###### Get Card Details
![Card Details Post Update](images/RequestResponse/GetCardDetails_AfterUpdate.PNG)

###### Delete Card
![Delete Card Request and Response](images/RequestResponse/DeleteCard.PNG)

###### Get Card Details
![Card Details Post Delete](images/RequestResponse/GetCardDetails_AfterDelete.PNG)

###### Card Validation - Success
![Card Validation Success](images/RequestResponse/CardValidation_Success.PNG)

###### Card Validation - Luhn Error
![Card Validation - Luhn Error](images/RequestResponse/CardValidation_LuhnError.PNG)

###### Card Validation - Card Type Error
![Card Validation - Card Type Error](images/RequestResponse/CardValidation_CardTypeError.PNG)

###### Card Validation - Expiry Error
![Card Validation - Expiry Error](images/RequestResponse/CardValidation_ExpiryError.PNG)

###### Card Validation - Length Error
![Card Validation - Length Error](images/RequestResponse/CardValidation_LengthError.PNG)

###### Card Validation - Blacklist Error
![Card Validation - Blacklist Error](images/RequestResponse/CardValidation_BlacklistError.PNG)