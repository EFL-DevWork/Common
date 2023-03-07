## Quick start instructions

1. Dockerized setup
   This will bring up payment service, bank & fraud services, basic infra[Postgres & ELK stack] on docker.
```
cd <your working folder>
make demo
```
NOTE: Executing the above make command
* exports DB_PASSWORD used for postgresDB
* exports KEY used for user details Encryption
* downloads `dev-infra` and `demo-bank-fraud-service` repositories
* as part of `dev-infra`
   * brings up basic infra - elasticsearch, logstash, kibana, postgres
   * brings up nginx-proxy, ca setup and dns setup for developer workflow with https and custom names to access services.
* as part of `demo-bank-fraud-service`
   * build bankservice and fraudservice docker images
   * run single instances of each bankservice and fraudservice.
   * setup postgres for bankservice
* copy the sample env for main payment service
* setup postgres for payment service
* will run single instance of payment service.

To run the go service locally instead of docker, you could use
```
//in this step use a github personal access token with read access
export ORG_GRADLE_PROJECT_gdusername=<<your_github_username>>
export ORG_GRADLE_PROJECT_gdtoken=<<your_github_token>>

devbox postgres up
devbox elk up
make deploy-local
```

Or to run the build locally
```
./gradlew -Pgdusername=<<your_github_username>> -Pgdtoken=<<your_github_token>> clean build

```

Explore the apis available on the swagger-ui [http://localhost:8080/swagger-ui.html]