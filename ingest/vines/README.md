# True Process - ViNES Ingest Service

This service enables you to ingest ViNES messages from a RabbitMQ server. 

# Dependencies

This service requires the following toolkits: 

  * com.ibm.streamsx.datetime
  * com.ibm.streamx.health.ingest

This service requires the following libraries: 

  * Apache Common CLI v1.2
  * Google Guava v21.0

# Expected Input

This service does not ingest any data.

# Output

  * **Published Topic Name:** "ingest-vines"
  * **Output JSON Schema:** [Observation Type](https://github.com/IBMStreams/streamsx.health/wiki/Observation-Data-Type)

```
{
  "patientId" : string,
  "device" : {
    "id" : string,
    "locationId" : string
  },
  "readingSource" : {
    "id" : string,
    "sourceType" : string,
    "deviceId" : string
  },
  "reading" : {
    "ts" : numeric,
    "readingType" : {
      "system" : string,
      "code" : string
    },
    "value" : numeric,
    "uom" : string
  }
}
```

# Build

To build this service, run: 
`gradle build`


# Execute

To submit this service your local instance, run the following:

	cd com.ibm.streamsx.health.vines
	./ingest-vines.sh -h <host:port> -u <username> -p <password> -q <queueName> [-e <exchange_name>] [-d]

The service requires the following submission time parameters: 

 * **hostAndPort** - specifies the host:port for the RabbitMQ server
 * **username** - specifies the username to connect to RabbitMQ
 * **password** - specifies the password to connect to RabbitMQ
 * **queue** - specifies the RabbitMQ queue
 * **exchangeName** - (Optional) specifies the RabbitMQ exchange name. Default is "". 


# Debug

To enable debug mode, set the *-d* flag when executing the `ingest-vines.sh` script.

This service exports data with the following topic: 

 * **ingest-vines** - Topic that publishes the Observation data. Downstream analytic applications should subscribe to this topic.
 * **ingest-vines-error** - If any errors occur while parsing the ViNES messages, the original ViNES message and the error details will be published to this topic.
 * **ingest-vines-debug** - For debugging purposes only. Raw ViNES messages are published to this topic. 
