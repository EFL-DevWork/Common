
UPDATE user details

PUT request to localhost:8080/user

```
{
	"userId":"narendra2",
	"firstName":"naren",
	"taxId":"tax_id3",
	"password":"*****",
	"lastName":"kumar",
	"dob":"26/05/1991"
}  
```

if user exists on given userId, will give below success response , 200 code

```
{
    "name": "naren kumar",
    "message": "User details for naren kumar UPDATED successfully"
}
```

Note:
- taxId will be in encrypted format when you query user table