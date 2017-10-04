# Fhir Ingest

The FHIR Ingest component contain a set of services for ingesting data from a FHIR server.
This component contains the following services:

* FhirObservationIngestService - service for ingesting observation records for a set of patient.  This service periodically fetches observation records for a set of patients.

# Dependencies

This service requires the following toolkits: 

  * com.ibm.streamx.health.ingest

This service requires the following libraries: 

  * [Hapi FHIR v2.5](http://hapifhir.io/index.html)

# Expected Input

**FhirObservationIngestService**

This service requires the following query parameters:
```
{
  "patientId" : string,
  "pageSize" : numeric,
  "startTime" : numeric,
  "endTime" : numeric
}
```
* patientId - the patient id to search the observation recrods for
* pageSize - how many records to retrieve from the server per page, default is 100.
* starTime - search for records that are last updated between the startTime and endTime.  Default is -1.  It searches for all records that are available.
* endTime - search for records that are last updated between the startTime and endTime.  Default is -1.  It searches for all records that are available.

These parmaeters can be submitted to the service in one of two ways:

* Pub/Sub - Clients may publish the query parameters to the **ingest-fhir-obx-patientIds** topic.  See `com.ibm.streamsx.health.fhir.FhirObxConnector`
* Properties file - Specify a list of patients to query for in the `service.properties` file

# Output

**FhirObservationIngestService**

  * **Published Topic Name:** "ingest-fhir-box"
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

The service properties can be set in the `service.properties` file. The following properties are available:

| Property | Description | Default |
| --- | --- | :---: |
| baseurl | The base URL of the fhir server | None, this property must be specified. |
| patienids | Comma-separated list of patient ids to query observations for | None |
| period | The period between each query to the FHIr server in seconds | 10 seconds |
| debug | Debug flag for the service, turn debug tracing on/off | False |
| streamscontext | Job submission context of the service | Distributed |
| vmargs | VM arguements to pass to the service when it is being submitted | None |

Run the following command to execute the Vines Ingest Service: 

`gradle execute`

# Debug

To enable debug mode, set the **debug** property to `true` in the `service.properties` file.

The following test services are created to help debug the service.

* PrintFhirDebugRawMessagesService
    * Print raw messages from a FHIR server
    * To run:  `gradle execute_test_debug`
* PrintFhirObxErrorService 
    * Print out exception and associated records when a parse error has occured
    * To run:  `gradle execute_test_error`
* PublishPatientId - 
    * Dynamically submit query parameters (patient id, page size, start and end dates) to the FhirObservationIngestService
    * To run:   execute_test_patientid

This service exports data with the following topic: 

 * **ingest-fhir-obx** - Topic that publishes the Observation data. Downstream analytic applications should subscribe to this topic.
 * **ingest-fhir-error** - If any errors occur while parsing the FHIR messages, the original FHIR message and the error details will be published to this topic.
 * **ingest-fhir-debug** - For debugging purposes only. Raw FHIR messages are published to this topic. 
