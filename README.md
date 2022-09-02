# Streams Healthcare Analytics Platform

Welcome to the Streams Healthcare Analytics Platform!

Our goal is to make it easy to create real-time healthcare analytics application using IBM Streams.  We want our users to be able to rapidly develop, test and validate healthcare analytics. Researchers and clinicians should focus on the analytics part of an application, while the platform should take care of the necessary plumbing and infrastructure work.

## Getting Started

**NEW Release v0.1 is now available [here](https://github.com/IBMStreams/streamsx.health/releases/tag/v0.1)!**  See this [post](https://github.com/IBMStreams/streamsx.health/wiki/First-Release-Overview) to learn more about this release!

Follow the [**Getting Started Guide**]https://github.com/IBMStreams/streamsx.health/wiki/Getting-Started) to learn about how to leverage the services from the Streams Healthcare Analytics Platform

### Streams QSE ###
This is necessary if you are running on a system not supported by Streams.
1. Install [Streams QSE](http://ibmstreams.github.io/streamsx.documentation/docs/4.3/qse-intro/).

### Endpoint-monitor ###
Original repository: https://github.com/IBMStreams/endpoint-monitor  
Updated repository: https://github.com/kenguo573/endpoint-monitor  
Some of the UI functionalities are provided through HTTP operators, and they won't work without setting up an endpoint-monitor.
1. SSH into a System-S engineering node.
2. Clone / pull from the [streams.helm-charts](https://github.ibm.com/IBM-Streams/streams.helm-charts) repository, and add the `bin` directory to your PATH.
3. Log onto an Openshift cluster using `oc login`, switch to the namespace you want to use using `oc project`.
4. Set up a [streams user secret](https://github.com/kenguo573/endpoint-monitor#1-define-streams-user) if one does not exist.
5. Deploy the app using the following:

        oc new-app \
         -f https://raw.githubusercontent.com/kenguo573/endpoint-monitor/master/openshift/templates/streams-endpoints.json \
         -p NAME=<name of the app> \
         -p STREAMS_INSTANCE_NAME=<name of the instance that test sabs will be submitted to> \
         -p JOB_GROUP=<name of the job group that test sabs will be submitted to (do not use `default`)> \
         -p STREAMS_USER_SECRET=streams-user
      You can check the status using `oc get pods | grep <app name>` to see the build and deploy pods.
6. Once the deploy pods have finished running, you should see the service pods.

        -bash-4.2$ oc get pods | grep test-proxy
        test-proxy-1-9rgmw                                2/2       Running     0          5m
        test-proxy-endpoint-monitor-1-build               0/1       Completed   0          6m
        test-proxy-nginx-1-build                          0/1       Completed   0          6m
7. Run `oc get svc <app-name>`, and take note of the second port number.

        -bash-4.2$ oc get svc test-proxy
        NAME         TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
        test-proxy   NodePort   172.30.64.165   <none>        8443:31031/TCP   5m
8. On your browser, go to https://syss161.pok.stglabs.ibm.com:port-number (31031 in my case). If you see the `IBM Streams endpoint-monitor` page then continue to the next step.

### Health toolkit ###
1. You will need the [streamsx.inetserver](https://github.com/IBMStreams/streamsx.inetserver) toolkit which is not included in the Streams QSE docker image. Download the latest release and place the toolkit inside the STREAMS_SPLPATH directory.
2. Clone this repository into the Streams QSE docker container.
3. Run `gradlew build` in the project root directory.

### Building and launching a demo ###
1. On a terminal inside the docker container, go into the /samples/PatientsMonitorDemo directory.
2. Run `../../gradlew execute`.
3. SABs as well as their configuration files will be produced in the demo directory. Submit these to the Streams instance provided when setting up the endpoint-monitor. Submit the UIService job to the job group provided earlier.
4. Wait a few moments, then go to https://syss161.pok.stglabs.ibm.com:port-number/streams/jobs/job-number/health/ to see the UI.

## Streams Healthcare Demos

### Python Jupyter Notebook Demo

As part of our initial work for this platform, we have developed a real-time ECG monitoring sample, using the Physionet Ingest Service, Python and Jupyter notebook.  

[<img src="https://github.com/IBMStreams/streamsx.health/blob/master/samples/HealthcareJupyterDemo/images/Healthcare_Demo.png" alt="Healthcare Jupyter Notebook Demo" width="600">](https://github.com/IBMStreams/streamsx.health/blob/master/samples/HealthcareJupyterDemo/)

To see this sample in action, you can run this sample using [**IBM Data Science Experience**](https://datascience.ibm.com/) and [**Streaming Analytics Service**](https://console.ng.bluemix.net/docs/services/StreamingAnalytics/index.html) on Bluemix.  See this [notebook](https://datascience.ibm.com/exchange/public/entry/view/9fc33ce7301f10e21a9f92039cad29a6
) for details.

To run this sample in Streams Quick Start Edition:

1.  Get the Streams Quick Start Edition VM from [here](https://www-01.ibm.com/marketing/iwm/iwm/web/preLogin.do?source=swg-ibmistvi&S_TACT=000000VP&S_OFF_CD=10000737).
1.  Clone this repository.
1.  Follow the instructions from here to run the demo:  [Healthcare Python Streaming Application Demo](https://github.com/IBMStreams/streamsx.health/tree/master/samples/HealthcareJupyterDemo)

### Population Health and Patient Monitoring

This sample demonstrates how we can use IBM Streams and the Streams Healthcare Anallytics Platform to monitor patient status in real-time. The sample generates vitals and ECG data for 100 patients. Patient data is fed into an analytics application that checks if a patient's vitals are in the normal range. If the vitals exceed the normal ranges, an alert is raised and is displayed on the dashboard.

[<img src="https://github.com/IBMStreams/streamsx.health/blob/develop/samples/PatientsMonitoringDemo/images/patientsMonitoring.jpeg" alt="Population Health and Patient Monitoring" width="600">](https://github.com/IBMStreams/streamsx.health/tree/develop/samples/PatientsMonitoringDemo)

To run this sample in Streams Quick Start Edition:

1.  Get the Streams Quick Start Edition VM from [here](https://www-01.ibm.com/marketing/iwm/iwm/web/preLogin.do?source=swg-ibmistvi&S_TACT=000000VP&S_OFF_CD=10000737).
1.  Clone this repository.
1.  Follow the instructions from here to run the demo:  [Population Health and Patient Monitoring Demo](https://github.com/IBMStreams/streamsx.health/tree/develop/samples/PatientsMonitoringDemo)


## Platform Design and Roadmap

[<img src="https://github.com/IBMStreams/streamsx.health/blob/wiki/img/healthroadmap.jpg" alt="Streams Healthcare Analytics Platform Roadmap" width="600">](https://github.com/IBMStreams/streamsx.health/blob/master/samples/HealthcareJupyterDemo/)

This diagram shows what we think a typical Streams healthcare application will look like and its major components.  The blue boxes represent components that should be provided by the platform.  The purple box represents an area where our end-user should focus on.  (i.e. developing advanced analytics).  

For details on the design and roadmap of this platform, please refer to here:

https://github.com/IBMStreams/streamsx.health/wiki

Our design and roadmap are always up for discussions and we welcome your feedback and contribution.  Please submit an [issue](https://github.com/IBMStreams/streamsx.health/issues) if you have any feedback for us.

## Repository Organization

The platform is designed to employ the microservice architecture.  A microservice is a small application written in SPL, Java, or Python that fulfills a specific task in a bigger healthcare application.  An application is made up of one or more of microservices, loosely connected to each other using the dynamic connection feature (Import/Export operators) in Streams.  To learn more about the microservice architecture in Streams, refer to this [post](https://developer.ibm.com/streamsdev/2016/09/02/analytics-microservice-architecture-with-ibm-streams/).

The repository is set up to accomodate this architecture.  The top level folders represent major functional components of the platform.  Under each folder, you will find one or more microservices for that component.  Each of the services can be built independently using gradle.  You can build them by following the build instructions below.  

To run the services, follow instructions as documented in their respective README.md files.

## Build Instructions

This repository is set up to build using [Gradle](https://gradle.org/).

All of the services can be built from the root folder by running **`gradle build`**.

If gradle is not installed on your system, the project is shipped with a gradle wrapper.  You can build the projects by using this wrapper and running **`gradlew build`**.

Similarly, individual components and  services can be built by navigating to either the component or service directory and running **`gradle build`**. 

All projects can be cleaned from either the root folder, a component folder or a service folder by running **`gradle clean`**

# The Contributors

Thank you to all our contributors.  This platform is made available from their contributions and valuable feedback/advises.

| Name | Company |
|------|------|
| Brandon Swink | [IBM](https://www.ibm.com/analytics/us/en/technology/stream-computing/) |
| Gergens Polynice | [CleMetric](http://www.clemetric.com/)
| James Cancilla | [IBM](https://www.ibm.com/analytics/us/en/technology/stream-computing/) |
| Jonathan Lachman  | [True Process](http://www.trueprocess.com/)|
| Peter Nicholls | [IBM](https://www.ibm.com/analytics/us/en/technology/stream-computing/) |
| Samantha Chan | [IBM](https://www.ibm.com/analytics/us/en/technology/stream-computing/) |
| Sharath Cholleti | [CleMetric](http://www.clemetric.com/)


[<img src="images/logo.png" alt="CleMetric" width="200">](http://www.clemetric.com/)      [<img src="images/TP-Logo-Default.png" alt="True Process" width="200">](http://www.trueprocess.com/)      [<img src="images/ibmpos_blue.jpg" alt="IBM" width="200">](https://www.ibm.com/analytics/us/en/technology/stream-computing/)         

## Learn more about Streams

To learn more about Streams:

* [IBM Streams on Github](http://ibmstreams.github.io)
* [Introduction to Streams Quick Start Edition](http://ibmstreams.github.io/streamsx.documentation/docs/4.1/qse-intro/)
* [Streams Getting Started Guide](http://ibmstreams.github.io/streamsx.documentation/docs/4.1/qse-getting-started/)
* [StreamsDev](https://developer.ibm.com/streamsdev/)
