# Natural Language Analytics Service

The **com.ibm.streamsx.health.analyze.nlp** project contains the following analytic microservices: 

  * **Annotate Diseases and Symptoms Service**: Given an `Observation` tuple, the service annotates data from the valueStr attribute with disease and symptoms information.

# Dependencies

  * This service requires the exported package (*.pear) from the Watson Content Analytics Symptoms and Diseases annotator. 
  * com.ibm.streamsx.nlp toolkit
  * com.ibm.streamsx.topology toolkit
  * com.ibm.streamsx.json toolkit


# Build

Run the following command to build all of the services: 

`gradle build`


## Expected Input

This service expects the `Observation` schema as the input data.  The valueStr attribute will be used as input to the Watson Content Analytics annotators.


## Output

### Publish Topic: "com/ibm/streamsx/health/analyze/nlp/services/annotateDiseaseService/diseasesAnnotations/v1"

  * **Output JSON Schema:** ObservationWithDiseaseAnnotation

    ```
    {
      "obx" : Observation,
      "annotation" : {
		conceptid: string,
		ccsCode: string,
		end: string,
		modifiers: string
		sofa: string,
		begin: string,
		section: string,
		xmi_0x3a_id: string, 
		dateInMilliseconds: string,
		modality: string, 
		normalized: string,
		icd9Code: string,
		date: string,
		hccCode: string,
		dateSource: string,
		icd10Code: string,
		ruleId: string,
		origin: string   
      }
    }
    ```

## Execute

The service properties can be set in the `service.properties` file. The following properties are available:

| Property | Description | Default |
| --- | --- | :---: |
| **topic** | Topic to subscribe observation data from | *none* |
| **pearFile** | Location of pear file exported from Watson Content Analytics.  Put this file in the etc directory and use relative path to specify location. | None |


Run the following command to execute the Annotate Symptoms and Diseases Service:

`gradle executeSymptomDisease`

To execute all services in this project

`gradle execute`

