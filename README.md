# Java Starter
[![CI](https://github.com/Regional-IT-India/catalyst-service-java-starter/actions/workflows/gradlebuild.yml/badge.svg?branch=master)](Regional-IT-India/catalyst-service-golang-starter/actions/workflows/build.yml)
![Status](https://github.com/Regional-IT-India/getting-started/blob/main/badges/stable.svg)

## Table Of Contents
  - [Introduction](#introduction)
    - [Aspects](#aspects)
  - [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Quick Start](#quick-start)
    - [Additional setup instructions](#additional-setup-instructions)
  - [Architecture of the Demo](#architecture)
  - [Demo API Docs](#demo-api-docs)

## Introduction
Java microservice boilerplate to quickly bootstrap microservices in java Spring Boot.

### Aspects
- [x] Externalized Configuration
- [x] Centralized configuration
- [x] Secret Management
- [x] Structured logging
- [x] Log aggregation, visualization and analytics
- [x] Metrics collection, custom metrics & visualization
- [x] Distributed Tracing
- [x] Resilience handling 
- [x] Masking PII in logging
- [x] Exception Handling  
- [x] Response and error code mapping
- [x] Tests
- [x] CI
- [x] Style checks
- [x] Vulnerability checks
- [x] Code coverage
- [x] Contract testing
- [x] Auth middleware
- [ ] JWT token based authentication
- [ ] Steps to remove the demo code

## Getting Started

### Prerequisites
- `Bash and Make`
- `Docker and Docker compose`
* We have tested our services on Colima on Mac. You could choose to use any CRI of your choice.

### Quick Start

```bash
# Clone the repository
$ git clone git@github.com:Regional-IT-India/catalyst-service-java-starter.git
$ cd catalyst-service-java-starter
```

- To bring up basic service quickly.
```bash
$ make demo
$ make seed-data # run after application is completely up and running
```

The above steps should bring up the main service, downstream services and let the demo payment service complete it's flow. 

### Additional setup instructions
* [Centralized logging](docs/centralized-logging.md)
* [Setting up a secure vault for sensitive config data](docs/vault.md)
* [Setting up a central config server](docs/central-config-server.md)
* [Setting up authentication](docs/keycloak.md)
* [Setting up metrics collection, aggregation and visualization](docs/metrics.md)
* [Setting up Distributed Tracing](docs/distributed-tracing.md)
* [Local Security Checks](docs/security.md)
* [Devbox Utility - OPTIONAL](docs/devbox.md)


## Architecture
[Read more about demo payment architecture](docs/architecture.md)

## Demo API Docs

Once the services are setup and running, the below endpoints are available. Refer to each API's page for details.

* [Do Payment](docs/api/create-payment.md) : `POST /payments`
* [List Payments](docs/api/get-payment.md) : `GET /payments`
* [Add Users](docs/api/add-user.md) : `POST /user`
* [Get Users](docs/api/get-user.md) : `GET /user`
* [Update Users](docs/api/update-user.md) : `PUT /user`