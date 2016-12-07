# Physionet Ingest Service

Ingest data from physionet.org.  To use this service, submit the following application:

* com.ibm.streamsx.health.ingest.physionet.service::PhysionetIngestService

This service requires the following submission parameter:

* record.name - indicates what record to retrieve from Physionet.

This service exports data with the following topic:

* ingest-physionet

Data is exported as JSON with the following schema:

`type PublishJson_T = rstring jsonString;`

To consume data in SPL,  see com.ibm.streamsx.health.ingest.physionet.service.test::PhysionetIngestTest for details.
