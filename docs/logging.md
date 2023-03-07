#Logging

### Logging framework
Following are commonly used logging frameworks in Java. In this application we are using logback. Above frameworks 
natively implement slf4j.

1.Log 4j 2.x.

2.Logback.

### Log Levels - The log level indicates the severity or importance.
    1. Trace  - Detailed information than DEBUG.
    2. Debug  - Fine grained details of an event useful for debugging.
    3. Info   - Information messages to identify progress of application.
    4. Warn   - Warning messages, pottential harmful situation.
    5. Error  - Error/ Exception events.

## Text Logging
Text logging is printing log messages are plain text/string in log file/console.
The problem with text logging files is they are unstructured text data. This makes it hard to query them for any 
sort of useful information.

Hence, as a best practice always use structured logging

## Structured Logging
Structured logging can be thought of as a stream of key-value pairs/ JSON format for every event logged, instead of 
just the plain text line of conventional logging.
   
### Using Structured Logging

  1. #### Add JSON formatter and layout
    
     Create a logback-spring.xml and add appenders, configure json layout and json formatter.
     
     ``` 
     <configuration>
     <appender name="file-appender" class="ch.qos.logback.core.rolling.RollingFileAppender">
          <encoder class="net.logstash.logback.encoder.LogstashEncoder">
               <excludeMdcKeyName>accountNumber</excludeMdcKeyName>
               <jsonGeneratorDecorator class="net.logstash.logback.mask.MaskingJsonGeneratorDecorator">
                    <defaultMask>*****</defaultMask>
                    <path>/body/beneficiary/accountNumber</path>
                    <path>/body/payee/accountNumber</path>
               </jsonGeneratorDecorator>
          </encoder>
     </appender>
      <logger name="com.thoughtworks" level="info" additivity="false">
             <appender-ref ref="file-appender"/>
      </logger>
     </configuration>
     ```
  2. #### Logging events
  
     Log events for API calls should produce following log messages.
     ```json
     {
        "@timestamp":"2021-07-30T10:54:07.256+05:30",
        "@version":"1",
        "message":"POST /payments",
        "logger_name":"com.thoughtworks.filter.LoggingFilter",
        "thread_name":"http-nio-8080-exec-1",
        "level":"INFO",
        "level_value":20000,
        "trace_id":"85e0ccedb1eb9b6af47f57b4699f9848",
        "span_id":"cb34e56c4957a916",
        "event_code":"REQUEST_RECEIVED",
        "headers":"{\"content-length\":\"220\",\"postman-token\":\"089a01f5-bee6-4060-be51-83ceab380815\",\"host\":\"localhost:8080\",\"content-type\":\"application/json\",\"connection\":\"keep-alive\",\"accept-encoding\":\"gzip, deflate, br\",\"user-agent\":\"PostmanRuntime/7.26.8\",\"accept\":\"*/*\"}",
        "params":"{}",
        "body":{
           "amount":10001,
           "payee":{
             "name":"Eric",
             "accountNumber":"*****",
             "ifscCode":"AXIS1234"
           },
           "beneficiary":{
             "name":"Oman",
             "accountNumber":"*****",
             "ifscCode":"AXIS1234"
           }
        }
     }

     ```
  3. #### Setting MDC attributes.
  
     Logback provides an MDC map to set Trace/Span ids of request and other diagnostic attributes.
     
     ```
       MDC.put("trace_id", String.valueOf(UUID.randomUUID()));
       log.info("successfully created");
       MDC.clear();
     ```   

### Verify Logging

* Once the paymentservice is running with postgres and elk, make payment request and follow below steps to check logs in kibana
   * GO to https://kibana.my.devbox, it will redirect to kibana home page
   * In kibana home page, go to `Use Elasticsearch Data` and click on `Connect to your Elasticsearch index`
      * Step1: In "Create index pattern" copy the logstash index pattern from list of index patterns and paste it `index-pattern` and click on 'Next Step'
      * Step2: In "Cofigure Settings" click on dropdown list of `Time Filter field name` and select `@timestamp` and click on 'Create index pattern'
      * Go to Navigation Bar and click on `discover` icon
      * You can see all logs of paymentservice
      * To see logs respective to payment request, go to the Search Bar and type 'payment'/'payment post' and click on refresh