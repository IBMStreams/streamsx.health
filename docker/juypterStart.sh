#!/usr/bin/env bash
# docker run -i -t -p 8888:8888 streamhealth bash -c "conda install jupyter -y --quiet && mkdir /opt/notebooks && jupyter notebook --notebook-dir=/opt/notebooks --ip='*' --port=8888 --no-browser"
# docker run -itp 8888:8888 -v /Users/siegenth/Development/JETBRAINS/PYCHARM/streamsx.health/docker:/app --env-file=env_file streamhealth bash -c "conda install jupyter -y --quiet && jupyter notebook --notebook-dir=/app/HealthcareJupyterDemo/notebooks --ip='*' --port=8888 --no-browser"
#docker run -itp 8888:8888 -v /Users/siegenth/Development/JETBRAINS/PYCHARM/streamsx.health/docker/HealthcareJupyterDemo/notebooks:/app/notebooks --env-file=env_file streamhealth bash -c "conda install jupyter -y --quiet && jupyter notebook --notebook-dir=/app/notebooks --ip='*' --port=8888 --no-browser"
#docker run -itp 8888:8888 -v ${PWD}/HealthcareJupyterDemo/notebooks:/app/notebooks --env-file=HealthcareJupyterDemo/notebooks/env_file streamhealth bash -c "conda install jupyter -y --quiet && jupyter notebook --notebook-dir=/app/notebooks --ip='*' --port=8888 --no-browser"
NOTEBOOKS=${PWD}/HealthcareJupyterDemo/notebooks

#docker run -itp 8888:8888 -v ${PWD}/HealthcareJupyterDemo/notebooks:/app/notebooks --env-file=HealthcareJupyterDemo/notebooks/env_file streamhealth bash -c "conda install jupyter -y --quiet && jupyter notebook --notebook-dir=/app/notebooks --ip='*' --port=8888 --no-browser"
docker run -itp 8888:8888 -v ${NOTEBOOKS}:/app/notebooks --env-file=${NOTEBOOKS}/env_file streamshealth bash -c "conda install jupyter -y --quiet && jupyter notebook --notebook-dir=/app/notebooks --ip='*' --port=8888 --no-browser"


