#!/bin/bash

$STREAMS_INSTALL/java/jre/bin/java \
-Dfile.encoding=UTF-8 \
-classpath com.ibm.streamsx.health.hapi.jar:$STREAMS_INSTALL/lib/com.ibm.streams.operator.jar:\
$STREAMS_INSTALL/lib/com.ibm.streams.operator.samples.jar:$STREAMS_INSTALL/ext/lib/commons-math-2.2.jar:\
$STREAMS_INSTALL/ext/lib/log4j-1.2.17.jar:$STREAMS_INSTALL/ext/lib/JSON4J.jar:\
opt/lib/hapi-base-2.2.jar:\
opt/lib/com.ibm.streamsx.health.ingest.jar:\
opt/lib/hapi-structures-v21-2.2.jar:opt/lib/hapi-structures-v22-2.2.jar:opt/lib/hapi-structures-v23-2.2.jar:\
opt/lib/hapi-structures-v231-2.2.jar:opt/lib/hapi-structures-v24-2.2.jar:\
opt/lib/hapi-structures-v25-2.2.jar:opt/lib/hapi-structures-v251-2.2.jar:\
opt/lib/hapi-structures-v26-2.2.jar:opt/lib/log4j-1.2.17.jar:opt/lib/slf4j-api-1.6.6.jar:\
opt/lib/slf4j-log4j12-1.6.6.jar:$STREAMS_INSTALL/toolkits/com.ibm.streamsx.topology/opt/apache-mina-2.0.2/dist/mina-core-2.0.2.jar:\
$STREAMS_INSTALL/toolkits/com.ibm.streamsx.topology/lib/com.ibm.streamsx.topology.jar $1 $2 com.ibm.streamsx.health.hapi.services.AdtIngest
