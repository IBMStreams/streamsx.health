# MLLP and HL7 Support

The initial contribution consists of:

* MLLP Server - listening for incoming connection from on a node on a specified port
* HL7 Parser for ORU01 Messages - parser for parsing ORU01 messages and transforming them to SPL Tuples.  ORU01 messages represent observations gathered from devices, lab results, etc.
* HL7 Parser for ADT Messages - parser for parsing ADT messages and extracting information from MSH, EVN, PID and PV1 segments.  Results are published as AdtEvent.

When a message is recieved by the server, the server transforms the messages to SPL Tuples.  The SPL stream is then published with a specified topic.  Downstream applications can subscribe to that topic to analyze the ingested messages.

This contribution contains two projects:

1.  com.ibm.streamsx.health.hapi - This project contains the microservice for ingesting HL7 messages using the MLLP protocol.  The service is implemented using the Java Application API from the streamsx.topology project.  This project depends on the [HAPI](http://hl7api.sourceforge.net) project, which provides the underlying support for MLLP and HL7 Message Parsing.
2.  com.ibm.streamsx.health.spl - This is a SPL project, demonstrating how to ingest data from the microservice.

# Getting Started

## Downloading Dependencies

1.  Download the HAPI project from here:  [HAPI Download](https://sourceforge.net/projects/hl7api/files/hl7api/)  The health toolkit is built on v2.2.
2.  Extract HAPI to your file system.

## Setting up Environment Variables:

Set the following environment variables before starting Streams Studio:

* export STREAMS_ZKCONNECT=`<zookeeper ensemble>`
* export STREAMS_DOMAIN_ID=`<name of the domain>`
* export STREAMS_INSTANCE_ID=`<name of instance to submit your job to>`

## Building the Microservice at Command Line

1.  `cd com.ibm.streamsx.health.hapi`
1.  `gradle build` - This steps download all required dependencies and will build a `release` folder that contains everything you need to run.

## Running the Microservice at Command Line

1.  `cd com.ibm.streamsx.health.hapi/release`
1.  There are two microservices:
    * `./run.sh` - This runs the OBXORU01 microservice Java code and submits the job to the streams instance as specified in your environment variable.  
    * `./runAdt.sh` - This runs the ADT microservice Java code and submits the job to the streams instance as specified in your environment variable.
1.  To customize the port and topic for the microservice, add the following VM arguments when running run.sh
    * -Dport=`<port number>`
    * -Dtopic=`<topic to publish to>`
1.  To check that the microservice is running correctly, run the com.ibm.streamsx.health.spl::IngestObservation application from the com.ibm.streamsx.health.spl project.  By default, this application subscribes to "oru01" as the topic.  Modify this to match the topic being published by the service.


## Building the Projects in Eclipse

1.  Fork this project
2.  Clone the project to your local file system.  e.g.  `git clone https://github.com/IBMStreams/streamsx.health.git`
3.  `gradle deps` - This will download dependencies and place them in the correct folder in your project
4.  In Streams Studio:
    1. File -> Import -> General -> Existing Projects Into Workspace
    2. Import the following projects:
        1.  com.ibm.streamsx.health.hapi
        2.  com.ibm.streamsx.health.spl
5.  You may see that the build path for com.ibm.streamsx.health.hapi is not set up correctly and that it cannot find com.ibm.streamsx.topology.jar.  This jar file can be found in <Streams Install>/toolkits/com.ibm.streamsx.topology/lib directory if you are running Streams v4.1.1.

## Running the application in Eclipse

To run the application in Streams Studio:

1.  Open OruR01Ingest or AdtIngest class from com.ibm.streamsx.health.hapi.services
2.  Create a Java launch configuration for this class.  
3.  In the launch configuration, optionally specify the following as the VM argument:
    * -Dport=`<port number>`
    * -Dtopic=`<topic to publish to>`
4.  Run this launch configuration.  This will generate the SPL application, and submit it to the instance as specified in the environment variables earlier.
5.  To check that the microservice is running correctly, run the com.ibm.streamsx.health.spl::IngestObservation application from the com.ibm.streamsx.health.spl project.  By default, this application subscribes to "oru01" as the topic.  Modify this to match the topic being published by the service.

# Dependencies

* HAPI - [http://hl7api.sourceforge.net](http://hl7api.sourceforge.net)
