* Initiate Payment
  Do a POST to http://localhost:8080/payments with json body as follows:
  ````
  {
  	"amount" : 10001,
  	"payee": {
  		"name":"Ethan",
  		"accountNumber":12345,
  		"ifscCode":"HDFC1234"
  	},
  	"beneficiary": {
  		"name":"May",
  		"accountNumber":67890,
  		"ifscCode":"HDFC1234"
  	}
  }
  ````
* The above POST should return a 201 response with following content
  ````
  {
      "statusMessage": "Payment done successfully",
      "paymentId": 1
  }
  ````