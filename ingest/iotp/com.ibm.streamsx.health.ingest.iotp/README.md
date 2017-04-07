# IOT Platform Ingest Service

This service ingest data from IOT paltform and convert it into JSON strings that can be analyzed by the platform.

# Dependencies

* com.ibm.streamsx.health.ingest
* com.ibm.streamsx.iot

# Expected Input

None


# Output

## topic
    
* The topic to publish the data as for downstream applications to consume data.  This service simply passes the JSON string through.  The expectation is that the JSON can be converted to one of the standard types in the healthcare platform.  Default: ingest-beacon

# Build 

* Under development

# Execute

* Import projct into Streams Studio to build, make sure the mentioned dependencies are accessbile as toolkit locations in Streams Studio.
* Download the IotPlatform microservice from  [here](https://github.com/IBMStreams/streamsx.iot/releases/download/v1.0.2/com.ibm.streamsx.iot.watson.apps.IotPlatform.sab)
* Submit the IotPlatform application to a Streams Instance
* Submit the com.ibm.streamsx.health.ingest.iotp::TransformIotData application to a Streams Instance


# Setting up for Development

* Import projct into Streams Studio to build, make sure the mentioned dependencies are accessbile as toolkit locations in Streams Studio.
