#!/bin/sh

bokeh serve --allow-websocket-origin=localhost:15000 2>&1 | tee bokeh_server.log &
jupyter-notebook --ip=`hostname`
