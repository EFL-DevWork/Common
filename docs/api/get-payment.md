*   List Payments 
    Do a GET http://localhost:8080/payments
    * This should return 200 response with following body
  ````
  [
      {
          "id": 1,
          "amount": 10001,
          "beneficiaryName": "May",
          "beneficiaryAccountNumber": 67890,
          "beneficiaryIfscCode": "HDFC1234",
          "payeeName": "Ethan",
          "payeeAccountNumber": 12345,
          "payeeIfscCode": "HDFC1234",
          "status": "success"
      }
  ]
  ````