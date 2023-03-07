## Summary
It is a best practice to externalise the configuration for the service. The basic setup of the starter-kit includes the options for specifying externalised config in a file, or command line or as environment variable.

When there are many services or many instances of even a single service, having a central configuration service is a better option. Other benefits include not having to rely on files in a dynamic container based environment, easier propagation of config updates and better security at once place for config data. 

To enable central configuration with starter-kit we employ spring config server as default.

## Setup
### 1. Setup configuration
Set the values you need to come from config server in the file `paymentservice-default.yml` under `tools/demo/configserver/config` and bring up the config server
For example
```
    FRAUD_URL: http://localhost:9002
```
### 2. Spin up config-server container 
#### Via docker compose
```shell
$ make docker-infra-config 
```
#### Or via [devbox](devbox.md)
```shell
$ devbox config-server deploy tools/demo/configserver/config/paymentservice-default.yml config/paymentservice-default.yml
$ devbox config-server up
```

You can access the configuration via http://localhost:8888/paymentservice/default or https://configserver.my.devbox/paymentservice/default if using devbox

### 3. Enable the config-server usage in .env file. 
```shell
CONFIG_SERVER_ENABLED=true
```

### 4. Restart the application
```shell
$ cd service-java-starter # run one from below commands
$ make boot-run # to run service locally
$ make docker-app #to run service on docker
```

You can access the config values in the application code the same way as you access application properties, however now they will be picked up from what you have specified in the config server. This is enabled by the spring.cloud.config.* application properties which point to the config server url and specify the access credentials.   

As you update the values in dev-infra/config/config-server/paymentservice-default.yml, the config server will automatically pick up the refreshed values. How-ever as of now the application itself (i.e., payment service) is not configured to listen to config server updates, so will require a restart (`./gradlew bootRun` or `make docker-app`)

For the same config key value the application applies the following precedence:
1. Config server
2. Any specific file if configured as property source (e.g. in src/main/ PaymentApplication.java) 
3. External config file  
4. Environment variable
5. Command Line
6. application yml file

## Config server related files in starter-kit
You can change how config server itself is configured and the values it holds using the following:

1. dev-infra/config-server - docker compose file to start config server. Port number and the credentials to access config server can be customized here

2. Update the .env file of the project with the config server url and credentials by setting the following env variables
```
    CONFIG_SERVER_ENABLED
    CONFIG_SERVER_URL
    CONFIG_SERVER_USERNAME
    CONFIG_SERVER_PASSWORD
```
   
3. tools/demo/configserver/config/paymentservice-default.yml - the file that holds the configuration data that will be exposed by the config server. deploy command copies the file to dev-infra/config/config-server/ .The path to this file is attached as a volume in the docker-compose file when starting config server. 

4. Alternatively you can configure config server to pick up the values from a file from other sources such as github using secure access and expose it via the config server end point (not included in starter-kit, pls. refer Spring Cloud config server for details)
