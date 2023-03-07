## Summary
Monitoring is a way to look into what your servers are doing in real time. Monitoring has a lot of benefits. The biggest benefit is avoiding reactive panics. You can get a head start on issues occurring in your servers or applications before your users are impacted. Along with this benefit, you can also use monitoring to figure out how to:
* Increase uptime.
* Improve hardware and software performance.
* Plan for the future by making the best use of your resources.

In start-kit we are using monitoring with prometheus and Grafana.
* Prometheus is an open source system that collects and manages server and application metrics. It can 
also be configured to notify your team when an issue arises.
* Grafana is an open source tool that allows you to easily visualize information.

## Setup
### 1. Setup configuration
Default dashboard for Grafana is in dashboard.json under tools/demo/grafana/provisioning/dashboards. Configure this file according to needs.
### 2. Spin up Prometheus and Grafana containers
#### Using docker compose
```shell
$ make docker-infra-metrics
```
#### Or via [devbox](devbox.md)
```shell
devbox metrics deploy tools/demo/grafana/provisioning
devbox metrics up
```

### 3. Run service if not running
No need to restart the service.
```shell
$ cd service-java-starter # run one from below commands
$ make boot-run # to run service locally
$ make docker-app #to run service on docker
```
### 4. Verify metrics via Grafana dashboard
* Go to postman
* Make some requests at http://localhost:8080/payments
* Go to http://localhost:3000 on browser
    * Username/Password as admin/admin and click on login
    * Go to Dashboards -> Manage
    * Click on 'Paymentservice' dashboard
