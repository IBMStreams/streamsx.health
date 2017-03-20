# Watson Explorer Analytic Services

The **com.ibm.streamsx.health.analytics.watsonexplorer** project contains the following analytic micro services: 

  * **Medication Event Service**: Given a patient ID and an optional date range, this service will query a Watson Explorer collection and retrieve all mentioned medications. For each instance of a medication that is retrieved, the service will submit a `ClinicalEvent` tuple. 
  
      **NOTE:** In order to use this service, the Watson Explorer collection must have the **Medication Solution** annotators applied, which are available in the [IBM Care Management](https://www.ibm.com/support/knowledgecenter/SSHJB3_6.2.0/com.ibm.curam.nav.doc/cm_kc_welcome.html) product. 

  * **Symptom Event Service**: Given a patient ID and an optional date range, this service will query a Watson Explorer collection and retrieve all mentioned symptoms. For each instance of a symptom that is retrieved, the service will submit a `ClinicalEvent` tuple. 
  
      **NOTE:** In order to use this service, the Watson Explorer collection must have the **Symptom Solution** annotators applied, which are available in the [IBM Care Management](https://www.ibm.com/support/knowledgecenter/SSHJB3_6.2.0/com.ibm.curam.nav.doc/cm_kc_welcome.html) product.


# Collection Requirements

The Watson Explorer collection must be created with the following features: 

  * There must be an index field called *patient_id* that stores the patient ID to search on
  * The *date* index field is expected to reflect the date that the document was generated. This field is used when searching for documents within a date range.


# Dependencies

  * Apache Commons CLI v1.3.1
  * The [com.ibm.streamsx.watsonexplore](https://github.com/IBMStreams/streamsx.watsonexplorer) toolkit


# Build

Run the following command to build all of the services: 

`gradle build`


# Medication Event Service

## Expected Input

```
{
  "patientId" : string // unique ID of patient (REQUIRED)
  "query" : string, // query string (OPTIONAL)
  "startDate" : string, // start date (inclusive), format: YYYY-MM-DD (OPTIONAL)
  "endDate" : string, // end date (exclusive), format: YYYY-MM-DD (OPTIONAL) 
}
```


## Output

### Publish Topic: "analytics-medication-event"

  * **Published Topic Name:** analytics-medication-event
  * **Output JSON Schema:** ClinicalNoteEvent

    ```
    {
      "patientId" : string,
      "eventType" : "medication",
      "eventName" : string, // the name of the medication
      "ts" : Numeric, // epoch milliseconds
      "value" : Numeric,
      "uom" : string,
      "source" : string
    }
    ```

## Execute

The service properties can be set in the `medication.service.properties` file. The following properties are available:

| Property | Description | Default |
| --- | --- | :---: |
| **host** | The host name used to access Watson Explorer | *none* |
| **port** | The port number of the REST service. | `8393` |
| **subscriptionTopic** | The name of the topic that the service should subscribe to. | `ingest-beacon` |
| **medicationCollection** | The name of the collection that contains the documents to query for medication events. | `health-medication` |
| **patientFieldName** | The index field name in the documents that contains the patient ID. | `patient_id` |
| **wexToolkitPath** | The path to the Watson Explorer toolkit. Running `gradle build` automatically downloads the Watson Explorer toolkit into the *etc/toolkits* directory. This field does not need to be changed unless a specific version of the Watson Explorer toolkit needs to be used. | `etc/toolkits/com.ibm.streamsx.watsonexplorer` |
| **debug** | Enables service tracing and launching a "Print" service to capture the output. This is useful for troubleshooting problems with the service | `false` |


Run the following command to execute the Medication Event Service:

`gradle executeMedicationService`


# Symptom Event Service

## Expected Input

```
{
  "patientId" : string // unique ID of patient (REQUIRED)
  "query" : string, // query string (OPTIONAL)
  "startDate" : string, // start date (inclusive), format: YYYY-MM-DD (OPTIONAL)
  "endDate" : string, // end date (exclusive), format: YYYY-MM-DD (OPTIONAL) 
}
```


## Output

### Publish Topic: "analytics-symptom-event"

  * **Published Topic Name:** analytics-symptom-event
  * **Output JSON Schema:** ClinicalNoteEvent

    ```
    {   
      "patientId" : string,
      "eventType" : "symptom",
      "eventName" : string, // the name of the symptom
      "ts" : Numeric, // epoch milliseconds
      "value" : Numeric,
      "uom" : string,
      "source" : string
    }   
    ```

## Execute

The service properties can be set in the `medication.service.properties` file. The following properties are available:

| Property | Description | Default |
| --- | --- | :---: |
| **host** | The host name used to access Watson Explorer | *none* |
| **port** | The port number of the REST service. | `8393` |
| **subscriptionTopic** | The name of the topic that the service should subscribe to. | `ingest-beacon` |
| **symptomCollection** | The name of the collection that contains the documents to query for symptom events. | `health-symptom` |
| **patientFieldName** | The index field name in the documents that contains the patient ID. | `patient_id` |
| **wexToolkitPath** | The path to the Watson Explorer toolkit. Running `gradle build` automatically downloads the Watson Explorer toolkit into the *etc/toolkits* directory. This field does not need to be changed unless a specific version of the Watson Explorer toolkit needs to be used. | `etc/toolkits/com.ibm.streamsx.watsonexplorer` |
| **debug** | Enables service tracing and launching a "Print" service to capture the output. This is useful for troubleshooting problems with the service | `false` |

Run the following command to execute the Symptom Event Service:

`gradle executeSymptomService`


