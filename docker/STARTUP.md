## Overview 

This is a quick walk through of bringing up Docker
with Juypter notebook. The Jupter notebook is used 
to compose and submit the Streams topology application. 

## Configure
In order not to lose the Juypter notebook files when the container is
delete, we'll bind mount ..docker/HealthcareJupyterDemo/notebook on the host
machine to the container. This is done through docker configutation tool. 

Assuming the project cloned into your home directory, set the Docker preferences | File Sharing 
to your home directory. In the case of my home  directory ('/Users/siegenth'), this is what my preferences looks like....

![File Sharing](images/fileSharing.jpg)

For changes to take place, docker must be reboot. 


## Build and Run

Within the docker directory invoke the scripts


Build the image. 
```bash
./juypterBuild.sh

```

Run Juypter in the container using the built image.
```bash
./juypterStart.sh
```
Juypter notebook is accessable from your browser using [link](http://localhost:8888)

## Walkthrough of 'buildVCAPenv' * IN process *
Walk through this file to creates an 'env_file'. 
The env_file, has the credentials used to submit to a Streams service running
in the Cloud. 

You only need to execute this once, 'Successfully'.


May need to stop/start Juypter container.

### Walkthrough of 'HealthcareDemo-Docker'.
This uses the 'env_file' above.

#### 1) Open the notebook
#### 2) Menu 'Cell' | 'Run All'


## Stopping container
control-c followed by contol-c



