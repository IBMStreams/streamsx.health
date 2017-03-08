Services for sending json messages from the Health toolkit to a Kafka Message Server.

The following services are available:
* PublishJsonToKafka - publish Json messages to a topic on the Kafka server
* SubscribeJsonFromKafka - subscribe JSON messages from a topic on the Kafka server

Set up:
* Follow the instructions [here](https://www.ibm.com/blogs/bluemix/2015/10/streaming-analytics-message-hub-2/) to create the consumer.properties, jaas.conf and producer.properties file.
* Place the files into the **etc** folder of this toolkit
* Build the two services:
	* `gradle build`
	
The services will be built into the `output` directory.

To clean the toolkit:  `gradle clean`