# Streams Healthcare Java Example Service

This project demonstrates how one can create a service to work with Streams Healthcare Analtyics paltform.  

## Directory Structure

Service has this directory structure:

* info.xml - toolkit information of the service
* build.gradle - gradle script for building service
* service.properties - contain properties for customizing the behavior of the service
* impl/java/src - java source code of one or more services
* opt/lib - third-party libraries required by the services

## Service

The service is written in Java, using the Streams Java Application API.  A service should adhere to the following guidelines:

* The service class should reside in a package with a *.service suffix. (e.g. com.ibm.streamsx.health.example.java.service)
* The service class should be named with at `Service` suffix. (e.g. ExampleHealthService)
* The service class should subclass from AbstractService.  Abstract service provides infrastructure support:
    * Handling of properties files for service customization
    * Adding third-party libraries (*.jar) from opt/lib directory to the application bundle
    * Handling of submission time parameters
    * Handling of job submission in different streams context
* Customization of a service should be done in a service.properties file.  All services should support the following properties.  These properties are handled by AbstractService by default.
    * debug
    * streamscontext
    * vmargs
* The service class is responsible for the following:
    * Creation of a topology.  This example is written purely in Java.  But this can be used to wrapper a service written in SPL.
    * Define additional properties a service may support and handling of the properties.
        
## Building a Service

A service is to be built using the build.gradle script.  The script is set up to build any Java code residing in the impl/java/src directory.
If a service require a third-party library, client is responsible to define these libraries in the build script.  The dependencies will be downloaded
by the build script and  stored in the opt/lib directory.  

## Executing a Service

build.gradle is set up to execute the service, using the **execute** target.  
Clients are expected to configure this target to identify the main class and jar file tor running the service.
Clients may also define additional execute targets if the project contains more than one service.