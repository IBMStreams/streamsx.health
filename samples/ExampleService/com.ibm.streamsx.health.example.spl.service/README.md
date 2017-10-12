# Streams Healthcare SPL Example Service

This project demonstrates how one can create a SPL service to work with Streams Healthcare Analtyics paltform.  The SPl service is compiled and run by a Java wrapper.  The Java wrapper prvoides a consistent user experience to the end user.

## Directory Structure

Service has this directory structure:

```
com.ibm.streamsx.health.example.service
  |
  |---- info.xml - toolkit information of the service
  |---- build.gradle - build script for the Java service wrapper
  |---- service.properties - contain properties to be passed to the service
  |---- com.ibm.streamsx.health.example.spl.service - SPL code of the microservice
  |---- impl/java/src - contains Java code of the service wrapper
  |---- opt/lib - contains third-party libraries required by the wrapper or SPL service
```

## Service

A SPL service should adhere to the following guidelines:

* The SPL main composite should reside in a package with a *.service suffix. (e.g. com.ibm.streamsx.health.example.spl.service)
* The SPL main composite should be named with at `Service` suffix. (e.g. ExampleSPLService)
* Customization of a service should be defined in a service.properties file.  All services should support the following properties.  These properties are handled by AbstractService by default.
    * debug
    * streamscontext
    * vmargs
* A service may define a set of custom properties.  Custom properties should be scoped and prefixed with the service's fully qualified name.  (e.g.  com.ibm.streamsx.health.example.spl.service.property1).   The scoping allow us to combine properties from multiple services into a single property file, without running into name collision.    
* Service employs a publish-subcribe model to receive or send data to another service.  The topic definition should follow these guidelines:
    * The topic be defined using the MQTT convention as noted by the Publish/Subscribe operator in the streamsx.topology toolkit. (e.g. /a/b/c/d)
    * The topic should begin with the name of the service, delimited by "/"
    * Next the topic should the data being published  
    * The last segment of the topic should be a version number.  
    * For example: /com/ibm/streamsx/health/example/spl/service/observations/v1
 * All services publish data in JSON format, to maximize service interoperability with different languages.
    
## Service Wrapper   

* The Java service wrapper is responsible for the following:
    * Creation of a topology.  The topology invokes the SPL main composite.
    * Define additional properties a service may support.
    * Passing the properties found in service.properties to the SPL application
        
## Building a Service

A service is to be built using the build.gradle script.  The script is set up to build any Java code residing in the impl/java/src directory.
If a service require a third-party library, client is responsible to define these libraries in the build script.  The dependencies will be downloaded
by the build script and  stored in the opt/lib directory.  

## Executing a Service

build.gradle is set up to execute the service, using the **execute** target.  
Clients are expected to configure this target to identify the main class and jar file for running the service wrapper.
Clients may also define additional execute targets if the project contains more than one service.

## Evolving a Service

A service's API is defined by the following:

* Version Number in info.xml
* Name of the Service
* Properties in the service.properties file
* Parameters defined in the SPL application
* Data publication topic
* Data schema of the data being published

To maintain backwards compatibility and avoid breaking downstream applications, developers should follow these guidelines when evolving a service:

|API     |Guideline |
|--------|----------|
|Version in info.xml | Follow toolkit versioning guideline |
|Name of Service | Cannot be changed after it is defined |
|Properties in service.properties | New properties can be added without breaking compatibility.  Removing property result in a breaking change. |
|Parameters defined in SPL application | New parameter can be addd without breaking compatility.  Removing a parameter result in a breaking change.|
|Data publication topic | Adding new segments to the topic name can be done without breaking compatibility.  Removing or renaming a segment is a breaking change.|
|Data Schema | New attributes can be added to the schema without breaking compatbility.  Removing or renaming an attribute is a breaking change.|
