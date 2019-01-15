## Overview 

This is a walkthrough to bring up a Docker image with a Jupyter notebook, within
the notebook an application is composed and submitted to a Streams.
instance in the Cloud. The Jupyter notebook, *HealthcareDemo-Docker*,  composes and submits the Streams 
topology application. This is based upon the code found in &LT;gitBase&GT;/samples/HealthCareJupyterDemo
refer to this path for details, *HealthcareDemoSetup-Docker* notebook sets up the environment for the walkthrough. 

The following diagram illustrates the running application. 
![File Sharing](images/withDocker.png)

- A Docker image with Python, Java and Streams modules installed. In addition is has Jupyter server running, which we'll 
interact with via notesbooks.
- The docker image is built. 
- The container is run, the Jupyter server is brought up.
- The Jupyter server is accessed from your local computer (laptop).
- The 'heathcarerDemotSetup-Docker' notebook walks through the creation and configuration of the Streams service.
- The 'healthcareDemo-Docker' notebooks submit the simulator and application from the Docker container.
- The the data is rendered on your local computer. 

Currently store to COS, the EventStore is still in process. 
- The data processed in the Cloud on Streams is stored in a EventStore. 
- WatsonStudio can access the Stored data and do Analysis to generate insights on the realtime data.






## Configure
In order not to lose the Jupyter notebook files when the container is
deleted, we'll bind mount ..docker/HealthcareJupyterDemo/notebook on the host
machine to the container. This is done through docker configutation tool. 

Assuming the project cloned into your home directory, set the Docker preferences | File Sharing 
to your home directory. In the case of my home  directory ('/Users/siegenth'), this is what my preferences looks like....

![File Sharing](images/fileSharing.jpg)

For changes to take place, docker must be rebooted. 


## Build and Run

Within the docker directory invoke the scripts


Build the image. 
```bash
./jupyterBuild.sh

```
Run Jupyter in the container using the built image.
```bash
./jupyterStart.sh
```
Jupyter notebook is accessable from your browser using [link](http://localhost:8888)

### Walkthrough of 'HealthcareDemoSetup-Docker' 
This file to creates an 'env_file'. The env_file has the credentials used to submit to a Streams service running
in the Cloud. 


You will need to stop/start the Jupyter container after completing this walkthrough. 

### Walkthrough of 'HealthcareDemo-Docker'.
This uses the 'env_file' created above.

1) Open the notebook
2) Menu 'Cell' | 'Run All'


## Stopping Jupyter container
control-c followed by contol-c



