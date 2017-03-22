# Ingest Common

This component contains toolkits and artifacts that are common acorss all ingest service.  This component contains:

* Ingest Foundational Toolkit (com.ibm.streamsx.health.ingest) - This toolkit contains the necessary types and connectors for ingest services to communicate with downstream services.  For more details about this toolkit, see this page: [Healthcare Ingest Service Framework](https://github.com/IBMStreams/streamsx.health/wiki/Ingest-Service-Framework)
* Healthcare Kafka Service (com.ibm.streamsx.health.ingest.kafka) - This toolkit contains services for sending data from the Streams Healthcare Analytics platform to a Kafka message server (e.g. MessageHub).  JSON data from the platform can be sent to a Kafka message server using these services.
