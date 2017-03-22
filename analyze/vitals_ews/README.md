# Early Score Warning (EWS) Service

This service calculates the early warning score for a patient.  This service provides two warning scores:
* [Early Warning Score (EWS)](https://en.wikipedia.org/wiki/Early_warning_score)
* [National Early Warning Score (NEWS)](https://www.rcplondon.ac.uk/projects/outputs/national-early-warning-score-news)

The rules for determining  patient's EWS are written on business rules using Operation Decision Manager (ODM).  
The rules are written in natural language and then translated to running SPL code using the ODM Rules compiler.

The business rules projects are stored the following projects:
* com.ibm.streamsx.health.analyze.ruels.model
* streamsx-health-analyze-rules-vitals

# Dependencies

This sample depends on the following microservices and toolkits:
* com.ibm.streamsx.health.ingest
* com.ibm.streamsx.json
* com.ibm.streamsx.topology
* com.ibm.streams.rulescompiler

# Build 

To build this sample, it is expected that your environment is set up to use the Java Application API from the com.ibm.streamsx.topology project.

The following environment variables need to be set.  The sample will work with Streams 4.2.0.0 and up:

STREAMS_SPLPATH=/opt/ibm/InfoSphere_Streams/4.2.0.0/toolkits
STREAMS_INSTALL=/opt/ibm/InfoSphere_Streams/4.2.0.0
STREAMS_DOMAIN_ID=StreamsDomain
STREAMS_INSTANCE_ID=StreamsInstance

To build:

```
git clone https://github.com/IBMStreams/streamsx.health.git
cd streamsx.health
./gradlew build
```
This will take a few minutes for the Healtcare Streaming Platform to build.

# Development

You can update the rules using Streams Studio.  To import the projects into Streams Studio:

1.  Add the toolkits as specified in the Dependencies section as Toolkits Locations in Streams Studio
1.  Import the projects:
    1.  File -> Import -> General -> Existing Projects into Workspace
    1.  Navigate to streamsx.health/analyze/vitals_ews
    1.  Import all the projects into Streams Studio
1.  Once the projects are imported, switch to the Rules Perspective in Streams Studio
1.  Open the streamsx-health-analyze-rules-vitals project to update the rules. 

To regenerate the SPL application.

1.  Select the streamsx-health-analyze-rules-vitals project in the Rule Explorer
1.  Right click -> Generate SPL Source File...
1.  Specify the following in the dialog:
    1.  SPL Project:  com.ibm.streamsx.health.analyze.rules.vitals.spl
    1.  SPL Namespace:  com.ibm.streamsx.health.analyze.rules.vitals.spl
    1.  Uncheck "Generate function definitions"
    1.  Check Overwrite existing files
    1.  Check Split rule set by package
    1.  Check Generate type definitions
1.  Click Generate

This will generate SPL code into the following composite in com.ibm.streamsx.health.analyze.rules.vitals.spl project:

* com.ibm.streamsx.health.analyze.rules.vitals::streamsx_health_analyze_rules_vitals

If you select to **Generate function definitions** and have your functions overwritten, it may result in compile errors
in your workspace.  To fix the compile error, make sure the setAlert and addToMessages functions are defined as follows:

```
public void incrementEwsScore(mutable Patient obj, int32 arg0) {
	obj.ewsScore += arg0;
}

public void incrementNewsScore(mutable Patient obj, int32 arg0) {
	obj.newsScore += arg0;
}

```

To update the rules, import these two projects as existing projects in Streams Studio.

The following project contains the generated SPL code of the ODM rules:
* com.ibm.streamsx.health.analyze.rules.vitals.spl

The EWSScoreService is the demo service to show how you can use the generated SPL code 

To build the service:

1.  `cd com.ibm.streamsx.health.analyze.rules.vitals.spl`
1.  `gradle build`

# Execute

* The build EWSScoreService SAB file will be located in the **output** directory under **com.ibm.streamsx.health.analyze.rules.vitals.spl** project.  
* Submit the *.sab file to a running Streams instance.
