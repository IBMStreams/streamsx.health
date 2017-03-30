# QRS Detector Service

This service analyzes an ECG wave and submits a tuple indicating the beginning of a QRS complex. 


# Dependencies

This service depends on the following toolkits: 

  * com.ibm.streamsx.health.ingest
  * com.ibm.streamsx.json
  * com.ibm.streams.timeseries

This service depends on the following libaries:

  * OSEA4J.jar (https://github.com/MEDEVIT/OSEA-4-Java)


# Expected Input

This service expects an `Observation` tuple as the input: 

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

# Output

  * **Published Topics:** "analyze-qrsdetector"
  * **Output JSON Schema:** 

```
{
  "ts" : numeric (epoch),
  "value" : numeric,
  "qrsTimestamp" : numeric,
  "qrsDelay" : numeric, 
  "qrsClassification" : string,
  "rrMinusOneInterval" : numeric,
  "rrInterval" : numeric
}
```


# Build

Run the following command to build this service: 

`gradle build`


# Execute

The service properties can be set in the `qrsdetector.service.properties` file. The following properties are available: 

| Property | Description | Default |
| --- | --- | :---: |
| readingTypeCode | The readingType.code value that contains the ECG signal to analyze. | `X100-8` (ECG Lead I) |
| samplingRate | The sampling rate of the ECG signal being analyzed | `125` |
| subscriptionTopic | The topic to subscribe to | `ingest-beacon` |

Run the following command to launch the the QRS Detector Service:

`gradle execute`

