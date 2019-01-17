##
FROM ibmjava:8-sdk
## Install bzip for Anaconda & git

RUN apt-get update -y && \
apt-get install bzip2 && \
apt-get install git -y

WORKDIR /ins
# ADD  Anaconda3-4.2.0-Linux-x86_64.sh .
RUN wget https://repo.continuum.io/archive/Anaconda3-4.2.0-Linux-x86_64.sh
RUN bash ./Anaconda3-4.2.0-Linux-x86_64.sh -b -p /anaconda3
ENV PATH /anaconda3/bin:${PATH}

WORKDIR /app
ADD HealthcareJupyterDemo/package package
ADD HealthcareJupyterDemo/configcloud configcloud

RUN pip install --upgrade pip && \
pip install streamsx && \
pip install streamsx.messagehub && \
pip install streamsx.objectstorage && \
pip install redis && \
pip install -e package && \
pip install -e configcloud

#RUN pip install jupyterlab








