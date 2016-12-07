# Streams Healthcare Analytics Platform

Welcome to the Streams Healthcare Analytics Platform!

Our goal is to make it easy to create real-time healtcare analytics application using IBM Streams.  We want our users to be able to rapidly develop, test and validate healthcare analytics. Researchers and clinicians should focus on the analytics part of an application, while the platform should take care of the necessary plumbing and infrastructure work.

![Streams Healthcare Analytics Platform](https://github.com/IBMStreams/streamsx.health/blob/wiki/img/healthroadmap.jpg)

This diagram shows what we think a typical Streams healtcare application will look like and its major components.  The blue boxes represent components that should be provided by the platform.  The purple box represents an area where our end-user should focus on.  (i.e. developing advanced analytics).  

For details on the design and roadmap of this platform, please refer to here:

https://github.com/IBMStreams/streamsx.health/wiki

Our design and roadmap are always up for discussions and we welcome your feecdback and contribution.  Please submit an [issue](https://github.com/IBMStreams/streamsx.health/issues) if you have any feedback for us.

## Repository Organization

The platform is designed to employ the microservice architecture.  A microservice is a small application written in SPL, Java, or Python that fulfills a specific task in a bigger healthcare application.  An application is made up of one of more of microservices, loosely connected to each other using the dynamic connection feature (Import/Export operators) in Streams.  To learn more about the microservice architecture in Streams, refer to this [post](https://developer.ibm.com/streamsdev/2016/09/02/analytics-microservice-architecture-with-ibm-streams/).

The repository is set up to accomodate this architecture.  The top level folders represent major functional components of the platform.  Under each folder, you will find one or more microservices for that component.  Each of the services can be built independently using gradle.  You can build them by following the build instructions below.  

To run the services, follow instructions as documented in their respective README.md files.

## Build Instructions

All of the services can be built from the root folder by running **`gradle build`**.

Similarly, individual components and  services can be built by navigating to either the component or service directory and running **`gradle build`**. 

All projects can be cleaned from either the root folder, a component folder or a service folder by running **`gradle clean`**

## Learn more about Streams

To learn more about Streams:

* [IBM Streams on Github](http://ibmstreams.github.io)
* [Introduction to Streams Quick Start Edition](http://ibmstreams.github.io/streamsx.documentation/docs/4.1/qse-intro/)
* [Streams Getting Started Guide](http://ibmstreams.github.io/streamsx.documentation/docs/4.1/qse-getting-started/)
* [StreamsDev](https://developer.ibm.com/streamsdev/)
