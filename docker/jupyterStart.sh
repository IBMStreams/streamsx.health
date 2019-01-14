#!/usr/bin/env bash
echo ${PWD}
docker run -itp 8888:8888 --env-file=HealthcareJupyterDemo/notebooks/env_file --volume ${PWD}/HealthcareJupyterDemo/notebooks:/app/notebooks streamshealth bash -c "conda install jupyter -y --quiet && jupyter notebook --notebook-dir=/app/notebooks --ip='*' --port=8888 --no-browser"
