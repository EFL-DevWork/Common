CREATE user

POST request to  localhost:8080/user

```
{
	"userId":"narendra2",
	"firstName":"naren",
	"taxId":"tax_id",
	"password":"*****",
	"lastName":"kumar",
	"dob":"26/05/1991"
}
```
will return response
```
{
    "name": "naren kumar",
    "message": "User details for naren kumar ADDED successfully"
}
```