# Debug Print Service

This service prints the JSON tuples from incoming data stream.  The service can be used to subscribe to any topic for printing out the data stream to the console.

# Dependencies

* com.ibm.streamsx.health.ingest

# Expected Input

## subscribe-topic
* **Topic**:  subscribe-topic - this is the topic to subscribe data from
* **Schema**: The service expects type `tuple<rstring jsonString>`

# Output

N/A

# Build 

`gradle build`

# Execute

`gradle execute`
