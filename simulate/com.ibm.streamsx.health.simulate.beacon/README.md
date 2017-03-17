# ECG Simulator

This service simulates vital and waveform data for one or more patients. The types of data being generated includes: 

  * Heart Rate
  * BP Systolic
  * BP Diastolic
  * Temperature
  * SpO2
  * Respiratory Rate
  * ECG Lead I

Each *vital* data (Heart Rate, BP, Temperature, SpO2) are generated at a rate of 10 samples/sec each. The **ECG Lead I** waveform is generated at a rate of 125 samples/sec. The **Respiratory Rate** is generated at a rate of 1 sample/sec.

The simulator produces data using the `Observation` type. The data is output using the **PublishConnector**, which outputs the `Observation` type in JSON. By default, the simulator generates data for a single patient, with a patient ID of "patient-1". The simulator can be configured to simulate more than 1 patient. Simulating data for multiple patients works as follows: 

  1. Generate values for vitals and waveforms for "patient-1"
  2. For each additional patient, duplicate the `Observation` record and replace the "patient-1" patient ID with another patient ID (i.e. patient-2).
  3. Publish the `Observation` tuples for all patients
  4. This process allows us to quickly generate data for 10s or 100s of patients while maintaining performance (since we don't need separate threads for each patient). 
    * The down-side to this approach is that since the `Observation` records are all copied, each patient will have the same set of vital and waveform values generated.


# Dependencies

  * org.apache.commons.lang3


# Expected Input

This service does not ingest any data. 


# Output

## Publish Topic: "ingest-beacon"

**Published Topic Name:** ingest-beacon
**Output JSON Schema:** [Observation Type](https://github.com/IBMStreams/streamsx.health/wiki/Observation-Data-Type)

```
{
  "patientId" : "string",
  "device" : {
    "id" : "string",
    "locationId" : "string"
  },
  "readingSource" : {
    "id" : "string",
    "sourceType" : "string",
    "deviceId" : "string"
  },
  "reading" : {
    "ts" : Numeric,
    "readingType" : {
      "system" : "string",
      "code" : "string"
    },
    "value" : Numeric,
    "uom" : "string"
  }
}
```

# Build

Run the following command to build the service: 

 `gradle build`


# Execute

Run the following command to compile the SAB and launch the service: 

 `gradle execute`
