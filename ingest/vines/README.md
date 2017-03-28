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

The service properties can be set in the `vines.service.properties` file. The following properties are available:

| Property | Description | Default |
| --- | --- | :---: |
| host | Specifies the host of the RabbitMQ server | _none_ |
| port | Specifies the port of the RabbitMQ server | `5672` |
| username | Specifies the username to connect to the RabbitMQ server | _none_ |
| password | Specifies the password to connect to the RabbitMQ server | _none_ |
| queue | Specifies the queue on the RabbitMQ server that contains the Vines messages | _none_ |
| exchangeName | Specifies the exchange name on the RabbitMQ server | _none_ |
| debug | Enables debug mode | `false` |

Run the following command to execute the Vines Ingest Service: 

`gradle execute`

# Debug

To enable debug mode, set the **debug** property to `true` in the `vines.service.properties` file.

This service exports data with the following topic: 

 * **ingest-vines** - Topic that publishes the Observation data. Downstream analytic applications should subscribe to this topic.
 * **ingest-vines-error** - If any errors occur while parsing the ViNES messages, the original ViNES message and the error details will be published to this topic.
 * **ingest-vines-debug** - For debugging purposes only. Raw ViNES messages are published to this topic. 
