# True Process - ViNES Ingest Service


This service enables you to ingest ViNES messages from a RabbitMQ server. 

To build this service, run: 

	gradle build

To generate the javadocs, run:
	
	gradle javadoc 

To submit this service your local instance, run the following:

	cd com.ibm.streamsx.health.vines
	./vines-ingest.sh -h <host:port> -u <username> -p <password> -q <queueName> [-e <exchange_name>]

The service requires the following submission time parameters: 

 * **hostAndPort** - specifies the host:port for the RabbitMQ server
 * **username** - specifies the username to connect to RabbitMQ
 * **password** - specifies the password to connect to RabbitMQ
 * **queue** - specifies the RabbitMQ queue
 * **exchangeName** - (Optional) specifies the RabbitMQ exchange name. Default is "". 

This service exports data with the following topic: 

 * ingest-vines
