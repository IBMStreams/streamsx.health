# Physionet Ingest Service

Ingest data from physionet.org.  To use this service, submit the following application:

* com.ibm.streamsx.health.ingest.physionet.service::PhysionetIngestService

This service requires the following submission parameter:

* record.name - indicates what record to retrieve from Physionet.

This service exports data with the following topic:

* ingest-physionet

Data is exported as JSON with the following schema:

`type PublishJson_T = rstring jsonString;`

To consume data in SPL, subscribe to the topic, the output schema of the susbribe operator should be PublishJson_T.

To process data, convert JSON to the following SPL type:

`type SignalECG_T = float64 ts, int32 ch1, int32 ch2 ;
type PatientECG_T = SignalECG_T, tuple<rstring patientId> ;`


See com.ibm.streamsx.health.ingest.physionet.service.test::PhysionetIngestTest for details.
