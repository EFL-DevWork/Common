# Centralized Log Management

## Introduction

Centralized Log Management (CLM) is a type of logging solution system that consolidates all of our log data and pushes it to one central, accessible, and easy-to-use interface. Not only does CLM provide multiple features that allow us to easily collect log information, but it also helps us consolidate, analyze, and view that information quickly and clearly.

CLM allows us to do more with our log data and manage it much more efficiently. We will have the ability to access the data we want in seconds rather than hours, weeks, or even days by manually searching through tons of logs. Taking advantage of centrally storing and analyzing our logs with a CLM program will make our organization more dynamic, profitable, and secure.

## Our Approach
**Filebeat:** It is a lightweight shipper for forwarding and centralizing log data. Installed as an agent on your servers, Filebeat monitors the log files or locations that you specify, collects log events, and forwards them either to Elasticsearch or Logstash for indexing.

**Logstash:** It receives logs from filebeat, processes them and sends to elastic search.

**Elasticsearch:** It receives the log from logstash, index and analyze the log and store it in searchable indices.  

**Kibana:** It collects the log from elastic search and visualizes it.


## Setup
***Note:*** 
Make sure that docker daemon on your machine have proper resources.
This is needed if elastic search is used for centralized logging.
```
Expected resources: 
  CPU: 6
  MEMORY: 5GB
```
### 1. Spin up ELK and file beat containers
#### Using docker compose
```shell
$ make docker-infra-elk
```
#### Or via [devbox](devbox.md)
```shell
$ devbox elk up
```

`NOTE: Logstash, elastic search and kibana services comes up with the docker compose or devbox as a single unit.`

`Logs cannot be aggregated if you run in service locally. i.e you need to run the service in docker compose or devbox environments.`

### 2. Verify centralized logging services
   
- We can check whether elastic search is running by visiting http://localhost:9200 or https://elasticsearch.my.devbox if using devbox. 
- We can check our kibana dashboard for generated logs at http://localhost:5601 or https://kibana.my.debox if using devbox.
* GO to https://kibana.my.devbox, it will redirect to kibana home page
 * In kibana home page, go to `Use Elasticsearch Data` and click on `Connect to your Elasticsearch index`
    * Step1: In "Create index pattern" copy the logstash index pattern from list of index patterns and paste it `index-pattern` and click on 'Next Step'
    * Step2: In "Configure Settings" click on dropdown list of `Time Filter field name` and select `@timestamp` and click on 'Create index pattern'
    * Go to Navigation Bar and click on `discover` icon
    * You can see all logs of paymentservice
    * To see logs respective to payment request, go to the Search Bar and type 'payment'/'payment post' and click on refresh

## Recommendations and best practices while choosing centralized logging options
- Logging framework should be capable of supporting different protocols and log formats.
- Aggregation of log data from different sources and different formats should be supported by the logging platform.
- Choosing data indices (for example, Elasticsearch) is very important for achieving  high throughput performance.
- Make sure to have clear identification of what information needs to be logged (have clear differentiation between metrics and logs).
    - Application should focus on business and technology stack specific logs. 
    - System and server logs should be aggregated and should be kept out of context of the application logging scope.
- It is always recommended to decouple the logging platform with the application code.
    - For example, application can write logs to a log file. A tool like Filebeat can pick up the logs periodically and send them to ELK stack for further analysis.
    - Sidecar containers can also be considered to offload logging capabilities in container-based environment like kubernetes.
- Make sure to opt for a logging platform which have default high availability and disaster recovery support.
- It is always recommended going for managed logging service provider (probably from different cloud vendors) instead of managing the logging infrastructure by ourselves because sometimes it becomes overhead to manage and maintain complex infrastructure with right security and configuration policies.

