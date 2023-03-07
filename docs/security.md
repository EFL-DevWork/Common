# Security Tools

## Owasp Dependency check

For Owasp dependency check add following plugin in build.gradle file
```
Plugin: id "org.owasp.dependencycheck" version "6.0.2"
``` 
Also add following function in build.gradle :
```
dependencyCheck {
failBuildOnCVSS = 7
suppressionFile = 'tools/dependency-check/dependency-check-known-issues.xml'
}
```

### Run dependency check :

Use following command to run OWASP dependency check 
```
make dependency-check
```

### Check report 

After running dependency check command in terminal reports will be available in following path of the project :
```
<project>/build/reports/dependency-check-report.html
```
NOTE: To fix issue of particular dependency upgrade its version other than a vulnerable version. 

## Spotbugs - SAST

For Spotbugs add following plugin in build.gradle file

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
