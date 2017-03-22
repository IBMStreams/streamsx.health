# Early Score Warning (EWS) Service

This service calculates the early warning score for a patient.  This service provides two warning scores:
* [Early Warning Score (EWS)](https://en.wikipedia.org/wiki/Early_warning_score)
* [National Early Warning Score (NEWS)](https://www.rcplondon.ac.uk/projects/outputs/national-early-warning-score-news)

The rules for determining  patient's EWS are written on business rules using Operation Decision Manager (ODM).  
The rules are written in natural language and then translated to running SPL code using the ODM Rules compiler.

The business rules projects are stored the following projects:
* com.ibm.streamsx.health.analyze.ruels.model
* streamsx-health-analyze-rules-vitals

To update the rules, import these two projects as existing projects in Streams Studio.

The following project contains the generated SPL code of the ODM rules:
* com.ibm.streamsx.health.analyze.rules.vitals.spl

The EWSScoreService is the demo service to show how you can use the generated SPL code 

To build the service:

1.  `cd com.ibm.streamsx.health.analyze.rules.vitals.spl`
1.  `gradle build`
