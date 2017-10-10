# R-Peak Detect Service

This service analyzes an ECG wave and submits a tuple indicating an RPeak Event, and also calcualtes the RR interval.
This service makes use of the ComplexEvent operator from the com.ibm.streams.cep toolkit.  Each reading is inspected one at a time until a peak is detected in the signal. When a peak is detected in the signal, the event is further analyzed to determine if the peak event is an R-Peak event.  By default, a R-Peak event is defined when the min and max value in a window of data is greater than 0.8v.  A window is established from the time when a rise in voltage is first detected, to the time when the peak is found and a decrease in voltage detected.  The algorithm does not look at how long it takes for this jump to happen, but can be improved to take this into acccount.  


# Dependencies

This service depends on the following toolkits: 

  * com.ibm.streamsx.topology
  * com.ibm.streamsx.health.ingest
  * com.ibm.streamsx.json
  * com.ibm.streams.cep

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

  * **Published Topics:** "analyze/rpeak/cep/r"
  * **Output JSON Schema:** 

```
{
  "ts" : numeric (epoch),
  "patientId" : string,
  "data" : numeric,
}
```

  * **Published Topics:** "analyze/rpeak/cep/rr"
  * **Output JSON Schema:** 

```
{
  "rr" : numeric,
  "patientId" : string,
  "events" : [{
    "ts" : numeric (epoch),
    "patientId" : string,
    "data" : numeric,
  }]
}
```

# Build

Run the following command to build this service: 

`gradle build`


# Execute

The service properties can be set in the `service.properties` file. The following properties are available: 

| Property | Description | Default |
| --- | --- | :---: |
| readingCode | The readingType.code value that contains the ECG signal to analyze. | `X100-8` (ECG Lead I) |
| topic | The topic to subscribe to | `ingest-beacon` |
| peakThreshold | Threshold for identifying a steep jump in voltage | 0.8 |

Run the following command to launch the service:

`gradle execute`

