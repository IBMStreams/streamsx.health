# Healtcare Kafka Services

This component contains two services that can publish and subscribe the JSON data from the Streams Healthcare Analytics Platform to a Kafka Messaging server. (e.g. IBM MessageHub on Bluemix)

# Dependencies

* com.ibm.streamsx.health.ingest
* com.ibm.streamsx.messaging

# Expected Input

## PublishJsonToKafka

    * This service subscribes to a topic as specified by a submission parameter `topic`
    * This service expects an input stream of `PublishJson` type from the com.ibm.streamsx.health.ingest toolkit.

## SubscribeJsonFromKafka

    * N/A


# Output

## PublishJsonToKafka

    * N/A

## SubscribeJsonFromKafka

    * This service publishes data to a topic as specified by a submission parameter `topic`
    * This service produces an output stream of `PublishJson` type from the com.ibm.streamsx.health.ingest toolkit.
    
# Set up to use service

* Follow the instructions [here](https://www.ibm.com/blogs/bluemix/2015/10/streaming-analytics-message-hub-2/) to create the consumer.properties, jaas.conf and producer.properties file.
* If you are not using MessageHub, follow the instructions [here](http://ibmstreams.github.io/streamsx.documentation/docs/4.2/messaging/kafka-operators-getting-started/) to configure the producer.properties or consumer.properties files.  
 * Place the files into the **etc** folder of this toolkit

# Build 

* `gradle build`

# Execute

* *.sab files will be generated into the output folder of the service.  Submit the *.sab file to a Streams Instance.

# Setting up for Development

* Make sure the dependent toolkits are available in Streams Studio in toolkit locations.
* File -> Import
* General -> Existing Projects Into Workspace...
* Navigate to the folder where these services are located and import these projects into Streams Studio.

# Clean

To clean the services:

* gradle clean
