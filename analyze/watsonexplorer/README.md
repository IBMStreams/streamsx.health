# Watson Explorer Analytic Services

The **com.ibm.streamsx.health.analytics.watsonexplorer** project contains the following analytic micro services: 

  * **Medication Event Service**: Given a patient ID and an optional date range, this service will query a Watson Explorer collection and retrieve all mentioned medications. For each instance of a medication that is retrieved, the service will submit a `ClinicalEvent` tuple. 
  
  **NOTE:** In order to use this service, the Watson Explorer collection must have the **Medication Solution** annotators applied, which are available in the [IBM Care Management](https://www.ibm.com/support/knowledgecenter/SSHJB3_6.2.0/com.ibm.curam.nav.doc/cm_kc_welcome.html) product. 

  * **Symptom Event Service**: Given a patient ID and an optional date range, this service will query a Watson Explorer collection and retrieve all mentioned symptoms. For each instance of a symptom that is retrieved, the service will submit a `ClinicalEvent` tuple. 
  
  **NOTE:** In order to use this service, the Watson Explorer collection must have the **Symptom Solution** annotators applied, which are available in the [IBM Care Management](https://www.ibm.com/support/knowledgecenter/SSHJB3_6.2.0/com.ibm.curam.nav.doc/cm_kc_welcome.html) product.


## Collection Requirements

The Watson Explorer collection must be created with the following features: 

  * There must be an index field called *patient_id* that stores the patient ID to search on
  * The *date* index field is expected to reflect the date that the document was generated. This field is used when searching for documents within a date range.


## Getting Started

To get started with using these services, perform the following steps: 

  1. Clone the repository to your local workspace
  2. Navigate to the *analytics/watsonexplorer/com.ibm.streamsx.health.analytics.watsonexplorer* directory
  3. Run the build command: `gradle build`
  
### Medication Event Service

To run the **Medication Event Service**, perform these steps: 

  1. Modify the `medication.service.properties` and set values for the missing properties (i.e. *host*, *port* and *subscription topic*)
  2. Launch the service by running the command: `gradle executeMedicationService`

### Symptom Event Service

To run the **Symptom Event Service**, perform these steps: 

  1. Modify the `symptom.service.properties` and set values for the missing properties (i.e. *host*, *port* and *subscription topic*)
  2. Launch the service by running the command: `gradle executeSymptomService`


