# Simulate Clinical Notes Service

The **com.ibm.streamsx.health.simulate.clinicalnotes** project contains the following analytic microservices: 

  * **Simulate Clinical Notes**: Given a directory to scan for clinical notes, the service reads each document one at a time and generates Observation data

# Dependencies

  * com.ibm.streamsx.datetime
  * com.ibm.streamsx.health.ingest
  * com.ibm.streamsx.json 
  * com.ibm.streamsx.toppology


# Build

Run the following command to build all of the services: 

`gradle build`


## Expected Input

None

## Output

### Publish Topic: "com/ibm/streamsx/health/simulate/clinicalnotes/observations/v1"

  * **Output JSON Schema:** Observation from com.ibm.streamsx.health.ingest

## Execute

The service properties can be set in the `service.properties` file. The following properties are available:

| Property | Description | Default |
| --- | --- | :---: |
| **notesDirectory** | The directory to scan for clinical notes | Root of data directory |


Run the following command to execute the service:

`gradle execute`

