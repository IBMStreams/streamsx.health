# Physionet Ingest Service

This **PhysionetIngestService** ingests data from a [Physionet](https://physionet.org/) database.

# Dependencies

* com.ibm.streamsx.health.ingest

# Expected Input

The service expects two submission parameters:

* recordName - the name of the Physionet database record to ingest data from.  If not specified, ingest data from the `mitdb/100` record
* frequency - the frequency at which to replay the data has.  The frequency parameter does not alter the timestamps on the signals.  This is only to control how fast the data is to be replayed.

# Output

## ingest-physionet

** Published Topic Name: ** ingest-physionet
** Published JSON Format: ** Observation type from com.ibm.streamsx.health.ingest

# Build 

`gradle build`

# Execute

*.sab file will be produced in the output directory.  Submit the *.sab file to a running instance with the required submission time parameters.

# Setting up for Development

Development can be done using Streams Studio:

1.  Add the listed dependencies as one of the toolkit locations in Streams Explorer in Streams Studio
1.  File -> Import...
1.  General -> Existing profject into workspace
1.  Navigate to the folder that contains the com.ibm.streamsx.health.ingest.physionet project 
1.  Import project into Studio and wait for build to complete.
1.  Launch com.ibm.streamsx.health.ingest.physionet.service::PhysionetIngestService

To test if data can be ingested correctly, run this test application:

1.  com.ibm.streamsx.health.ingest.physionet.service.test::PhysionetIngestTest

