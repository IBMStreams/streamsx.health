# True Process - ViNES Ingest Service


This service enables you to ingest ViNES messages from a RabbitMQ server. 

To build this service, run: 

	gradle build

To generate the javadocs, run:
	
	gradle javadoc 

To submit this service your local instance, run the following:

	cd com.ibm.streamsx.health.vines
	./ingest-vines.sh -h <host:port> -u <username> -p <password> -q <queueName> [-e <exchange_name>] [-d]

The service requires the following submission time parameters: 

 * **hostAndPort** - specifies the host:port for the RabbitMQ server
 * **username** - specifies the username to connect to RabbitMQ
 * **password** - specifies the password to connect to RabbitMQ
 * **queue** - specifies the RabbitMQ queue
 * **exchangeName** - (Optional) specifies the RabbitMQ exchange name. Default is "". 

To enable debug mode, set the *-d* flag when executing the `ingest-vines.sh` script.

This service exports data with the following topic: 

 * **ingest-vines** - Topic that publishes the Observation data. Downstream analytic applications should subscribe to this topic.
 * **ingest-vines-error** - If any errors occur while parsing the ViNES messages, the original ViNES message and the error details will be published to this topic.
 * **ingest-vines-debug** - For debugging purposes only. Raw ViNES messages are published to this topic. 
