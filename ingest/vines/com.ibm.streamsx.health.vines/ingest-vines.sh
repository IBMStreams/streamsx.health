#!/bin/sh
#*******************************************************************************
# Copyright (C) 2017 International Business Machines Corporation
# All Rights Reserved
#*******************************************************************************

pushd build
pwd
java -cp ./libs/*:$STREAMS_INSTALL/lib/*:$STREAMS_INSTALL/toolkits/com.ibm.streamsx.topology/lib/com.ibm.streamsx.topology.jar:output/lib/commons-cli-1.2.jar:../../../common/com.ibm.streamsx.health.ingest/lib/com.ibm.streamsx.health.ingest.jar com.ibm.streamsx.health.vines.service.VinesAdapterServiceRunner "$@"
popd
