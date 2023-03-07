## Summary
Secure code is the best code. Secure by design means that you bake security into your software design from the beginning.
Well-known security threats should drive design decisions in security architectures. Reusable techniques and patterns provide solutions for enforcing the necessary authentication, authorization, confidentiality, data integrity, privacy, accountability, and availability, even when the system is under attack.

To enable such security to starter-kit we employ keycloak. It offers everything a sophisticated user management tool needs – without having to log on repeatedly with every login and into every system-as well as system security, social logins, support for mobile apps and integration into other solutions. Keycloak have implementations to LDAP and Active Directory as well.

## Setup 
***NOTE:*** *The tests are not setup currently to work with authentication. So the build and tests will fail once you set security.type to 'keycloak' as mentioned in below steps. So pls. set this value only when doing a bootRun after successful build*

### 2. Setup username and password for keycloak
Update .env file with Keycloak username and password.
```shell
$ KEYCLOAK_USER=<some-username>
$ KEYCLOAK_PASSWORD=<some-password>
```
### 2. Spin up keycloak container
#### Using docker compose
```shell
$ make docker-infra-keycloak
```
#### Or via [devbox](devbox.md)
```shell
$ cd service-java-starter
$ devbox keycloak deploy tools/demo/keycloak/realm-export.json realm-export.json
$ devbox keycloak up
```

### 3. Generate client credentials
* Go to http://localhost:8000 or https://keycloak.my.devbox/ on browser
* Click on "Administration Console" (it will redirect to login page)
  * Input "Username/ password" as "admin/ admin" and click login
* Now update the keycloak configuration  
  * On left side go to configure-> clients
  * click on "servicestarterclient"
  * within "servicestarterclient" go to "Credentials" sub tab
    * click on regenerated secret 
    * copy that secret 
  
### 4. Update .env file 
```shell
$ SECURITY_TYPE=keycloak
$ KEYCLOAK_SECRET=<secret copied from above step>
```

### 5. Restart the application
```shell
$ cd service-java-starter # run one from below commands
$ make boot-run # to run service locally
$ make docker-app #to run service on docker
```

### 6. Testing Keycloak and authentication in application

* Getting the bearer token from payment service
  
  * Do a GET with http://localhost:8080/getKeycloakAccessToken with following query parameters
    * `client _id` as `servicestarterclient`
    * `client_secret` is secret which is copied  from keycloak
  * `host` as `http://keycloak:8080`
    * send the request and copy the bearer token from the GET response

* Verify the auth mechanism by making accessing the service from Postman 
  *  First attempt without auth (expected failure with unauthorized error)
    * Open postman and prepare a post request at http://localhost:8080/payments
    * Go to Body sub tab and add the following:
       * For gradle
        ````
            { "amount" : 500, "payee": { "name":"user1", "accountNumber":12345, "ifscCode":"HDFC1234" }, "beneficiary": { "name":"user2", "accountNumber":67890, "ifscCode":"HDFC1234" } }
        ````
       *  For docker
        ````
            { "amount" : 500, "payee": { "name":"user1", "accountNumber":12345, "ifscCode":"AXIS1234" }, "beneficiary": { "name":"user2", "accountNumber":67890, "ifscCode":"AXIS1234" } }
        ````
    * Click on send 
    * You will get HTTP response "401- unauthorized"
  *  Now attempt with auth (expected success)
    * Go to Auth sub tab 
      * Select "type" as "Bearer" from dropdown list
      * paste the copied bearer token from previous step
    * Back in the main tab, click on "send" to send the POST request (now with auth token)
    * This time, you will get an HTTP response "201-created" indicating a successful POST 


## Password usage in the starter-kit

The starter-kit externalises sensitive information such as passwords using secrets and environment variables. Please follow the recommended security best practices related to these while implementing your services. Additionally please take measures to ensure usage of strong passwords as per general security practices and specific guidelines as applicable for the solution under development. An indicative list of guidance on passwords is as follows:
```
One or more uppercase  characters
One or more numerical digits
One or more special characters
Minimum length of twelve characters
Disallow any part of the user identifiable information
Disallow dictionary words
Disallow last three passwords
```