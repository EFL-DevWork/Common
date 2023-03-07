# Distributed Tracing

Distributed tracing is the capability for a tracing solution to track and observe service requests as they flow through
distributed systems by collecting data as the requests go from one service to another. The trace data helps you 
understand the flow of requests through your microservices environment and pinpoint where failures or performance 
issues are occurring in the system—and why.

We are using Jaeger and OpenTelemetry for distributed tracing.2

## Setup
### 1. Spin up Jaeger container
#### Via docker compose
```shell
$ make docker-infra-tracing
```
#### Or via [devbox](devbox.md)
```shell
$ devbox jaeger up
```

### 2. Enable Tracing usage in .env file
```shell
$ JAEGER_ENABLED=true
```
Jaeger is enabled by default in downstream bankservice and fraudservice.
### 3. Restart the application
```shell
$ cd service-java-starter # run one from below commands
$ make boot-run # to run service locally
$ make docker-app #to run service on docker
```

### 4. Verify Tracing
* Make a request to any endpoint in the payment service
* In browser, go to http://localhost:16686 or https://jaeger.my.devbox if using devbox.
* Select the service as the `SERVICE_NAME` mentioned in .env
* Click on "Find Traces"

You can now see all the traces for the request made in jaeger ui