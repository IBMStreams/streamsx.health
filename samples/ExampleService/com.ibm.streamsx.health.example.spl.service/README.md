# Streams Healthcare SPL Example Service

This project demonstrates how one can create a SPL service to work with Streams Healthcare Analtyics paltform.  

## Directory Structure

Service has this directory structure:

* info.xml - toolkit information of the service
* com.ibm.streamsx.health.example.spl.service - contains SPL application code for the service
* build.gradle - gradle script for building service
* service.properties - contain properties for customizing the behavior of the service
* impl/java/src - java source code of the service wrapper
* opt/lib - third-party libraries required by the services

## Service

The service is written in SPL.  The Java topology application provides a wrapper to the SPL service to allow user to have a more consistent user experience when invoking the service.

A SPL service should adhere to the following guidelines:

* The SPL main composite should reside in a package with a *.service suffix. (e.g. com.ibm.streamsx.health.example.spl.service)
* The SPL main composite should be named with at `Service` suffix. (e.g. ExampleSPLService)
* Customization of a service should be done in a service.properties file.  All services should support the following properties.  These properties are handled by AbstractService by default.
    * debug
    * streamscontext
    * vmargs
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