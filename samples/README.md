# streamsx.health.samples

This folder contains samples built using the microservices from the Streams Healthcare Analytics Platform:

* HealthcareJupyterDemo - Real-time ECG analysis application using the Physionet Ingest Service, Python and Jupyter Notebook.  This sample is built using the [BioSPPy](http://biosppy.readthedocs.io/en/stable/) Signal Processing Libraries.
* PatientsMonitoringDemo - This demo shows how you can build an application using the Streams Healthcare Analytics Platform by mixing and matching the various services from the platform.  This demo is a dashboard that monitors the vitals for 100 patients simulated by the healthcare beacon service.
* PatientsMonitoringDemo.rules - This contains the Operation Deecision Manager rules project for the PatientsMonitoringDemo.  The rules are used to ensure that a patient's vitals are within normal range.  The rules will direct the application to raise an alert when the vitals have gone beyond the specified normal ranges in the rules.
* ExampleService - This contains examples on how to create a microservice to analyze the data ingested by the platform.

