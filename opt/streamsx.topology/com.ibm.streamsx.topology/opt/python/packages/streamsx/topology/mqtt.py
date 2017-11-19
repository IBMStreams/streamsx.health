# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016

"""
Publish and subscribe to MQTT messages.

Additional information at http://mqtt.org and
http://ibmstreams.github.io/streamsx.messaging
"""

from streamsx.topology.topology import *
from streamsx.topology import schema

class MqttStreams(object):
    """
    A simple connector to a MQTT broker for publishing
    string tuples to MQTT topics, and
    subscribing to MQTT topics and creating streams.
    
    A connector is for a specific MQTT Broker as specified in
    the configuration object config. Any number of  publish()and  subscribe()
    connections may be created from a single mqtt_streams connector.
     
    Sample use:
    ::
        topo = Topology("An MQTT application")
        # define configuration information
        config = {}
        config['clientID'] = "test_MQTTpublishClient"
        config['qos'] = int("1") #(needs to be int vs long)
        config['keepAliveInterval'] = int(20) (needs to be int vs long)
        config['commandTimeout'] = 30000 (needs to be int vs long)
        config['period'] = 5000 (needs to be int vs long)
        config['messageQueueSize'] = 10 (needs to be int vs long)
        config['reconnectionBound'] = int(20)
        config['retain'] = True
        config['password'] = "foobar"
        config['trustStore'] = "/tmp/no-such-trustStore"
        config['trustStorePassword'] = "woohoo"
        config['keyStore'] = "/tmp/no-such-keyStore"
        config['keyStorePassword'] = "woohoo"

        # create the connector's configuration property map
        config['serverURI'] = "tcp://localhost:1883"
        config['userID'] = "user1id"
        config[' password'] = "user1passwrd"
     
        # create the connector
        mqstream = MqttStreams(topo, config)
     
        # publish a python source stream to the topic "python.topic1"
        topic = "python.topic1"
        src = topo.source(test_functions.mqtt_publish)
        mqs = mqstream.publish(src, topic)

        # subscribe to the topic "python.topic1"
        topic = ["python.topic1", ]
        mqs = mqstream.subscribe(topic)
        mqs.print()


    Configuration properties apply to publish and
    subscribe unless stated otherwise.

    serverURI
         Required String. URI to the MQTT server, either
        tcp://<hostid>[:<port>]}
        or ssl://<hostid>[:<port>]}.
        The port defaults to 1883 for "tcp:" and 8883 for "ssl:" URIs.
    
    clientID
         Optional String. A unique identifier for a connection
        to the MQTT server.
        he MQTT broker only allows a single
        onnection for a particular  clientID.
        By default a unique client ID is automatically
        generated for each use of  publish() and subscribe().
        The specified clientID is used for the first
        publish() or subscribe() use and
        suffix is added for each subsequent uses.
    
    keepAliveInterval
        Optional Integer.  Automatically generate a MQTT
        ping message to the server if a message or ping hasn't been
        sent or received in the last keelAliveInterval seconds.  
        Enables the client to detect if the server is no longer available
        without having to wait for the TCP/IP timeout.  
        A value of 0 disables keepalive processing.
        The default is 60.
    
    commandTimeout
        Optional Long. The maximum time in milliseconds
        to wait for a MQTT connect or publish action to complete.
        A value of 0 causes the client to wait indefinitely.
        The default is 0.
    
    period
        Optional Long. The time in milliseconds before
        attempting to reconnect to the server following a connection failure.
        The default is 60000.
    
    userID
        Optional String.  The identifier to use when authenticating
        with a server configured to require that form of authentication.
    
    password
        Optional String.  The identifier to use when authenticating
        with server configured to require that form of authentication.
    
    trustStore
        Optional String. The pathname to a file containing the
        public certificate of trusted MQTT servers.  If a relative path
        is specified, the path is relative to the application directory.
        Required when connecting to a MQTT server with an 
        ssl:/... serverURI.
    
    trustStorePassword
        Required String when trustStore is used.
        The password needed to access the encrypted trustStore file.
    
    keyStore
        Optional String. The pathname to a file containing the
        MQTT client's public private key certificates.
        If a relative path is specified, the path is relative to the
        application directory. 
        Required when an MQTT server is configured to use SSL client authentication.
    
    keyStorePassword
        Required String when keyStore is used.
        The password needed to access the encrypted keyStore file.

    messageQueueSize
        [subscribe] Optional Integer. The size, in number
        of messages, of the subscriber's internal receive buffer.  Received
        messages are added to the buffer prior to being converted to a
        stream tuple. The receiver blocks when the buffer is full.
        The default is 50.
    
    retain
        [publish] Optional Boolean. Indicates if messages should be
        retained on the MQTT server.  Default is false.
    
    qos
        Optional Integer. The default
         MQTT quality of service used for message handling.
        The default is 0.

    """

    def __init__(self, topology, config):
        self.topology = topology
        self.config = config.copy()
        self.opCnt = 0

    def publish(self, pub_stream, topic):
        parms = self.config.copy()
        parms['topic'] = topic
        parms['dataAttributeName'] = "string"
        if (++self.opCnt > 1):
            # each op requires its own clientID
            clientId = parms['clientID']
            if (clientId is not None and len(clientId) > 0):
                parms['clientID'] = clientId + "-" + str(id(self)) + "-" + str(self.opCnt)
        # convert pub_stream outputport schema from spl po to spl rstring type
        forOp = pub_stream._map(streamsx.topology.functions.identity, schema.CommonSchema.String)
        op = self.topology.graph.addOperator(kind="com.ibm.streamsx.messaging.mqtt::MQTTSink")
        op.addInputPort(outputPort=forOp.oport)
        op.setParameters(parms)
        return None

    def subscribe(self, topic):
        parms = self.config.copy()
        if (parms['retain'] is not None):
            del parms['retain']
        parms['topics'] = topic
        parms['topicOutAttrName'] = "topic"
        parms['dataAttributeName'] = "string"
        if (++self.opCnt > 1):
            # each op requires its own clientID
            clientId = parms['clientID']
            if (clientId is not None and len(clientId) > 0):
                parms['clientID'] = clientId + "-" + str(id(self)) + "-" + str(self.opCnt)
        op = self.topology.graph.addOperator(kind="com.ibm.streamsx.messaging.mqtt::MQTTSource")
        oport = op.addOutputPort(schema=schema.StreamSchema("tuple<rstring topic, rstring string>"))
        op.setParameters(parms)
        pop = self.topology.graph.addPassThruOperator()
        pop.addInputPort(outputPort=oport)
        pOport = pop.addOutputPort(schema=schema.CommonSchema.String)
        return Stream(self.topology, pOport)
