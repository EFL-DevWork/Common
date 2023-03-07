# Best practices for service development
- [Configuration](https://github.com/cd-jump-start/service-starter/blob/master/best-practices.md#configuration)
- Structured Logging
- Instrumentation
- Monitoring
- Health checks
- Resilience
- Code base structure
- CI 
- CD
- Security in CI/ CD
- Local env dev env setup support
- Service Registry
- Packaging 
- Packaging with containers

## Configuration
Configurations for services specify initial settings and parameters for their runtime behaviour. These could be name-value pairs or more refined configuration settings with section and sub-section goruping. Config values could be specified in a file, passed via command line, read from environment variables,retrieved via a config service or other similar sources provided by the runtime framework or environment. They could be specified in a variety of formats including yaml, json, xml, and application property files. 

### Considerations
- Configuration values will be different in different environments
- Within one environment itself the values will need changes at runtime
- It should be possible to change the config values without a code update
- Service should assume exact location of config files; this should be supplied by the hosting runtime 
- It is better if the service is able to use the changed values without a restart

### Best Practices
- Read a config file from a path supplied at run time via command line or environment variables
- Read config values from environment variables
- Read config values from command line
- Read config values from a configuration service
- Listen to config changes or provide an endpoint to be invoked when config changes

