# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2017

"""
Python API to allow creation of streaming applications for
Streaming Analytics service on IBM® Bluemix cloud platform
and on-premises IBM Streams.

Overview
########

IBM® Streams is an advanced analytic platform that allows user-developed applications to quickly ingest,
analyze and correlate information as it arrives from thousands of real-time sources.
Streams can handle very high data throughput rates, millions of events or messages per second.
With this API Python developers can build streaming applications that can be executed using IBM Streams,
including the processing being distributed across multiple computing resources (hosts or machines) for scalability.

Creating Applications
#####################

Applications are created by declaring a flow graph contained
in a :py:class:`~Topology` instance.

For details see :py:mod:`streamsx.topology.topology`.

Microservices
#############

Publish-subscribe provides the abiltity to connect streams between independent
IBM Streams applications regardless of their implementation language.
This allows a `microservice approach <https://developer.ibm.com/streamsdev/2016/09/02/analytics-microservice-architecture-with-ibm-streams/>`_
where a Streams application acting as a service publishes one
or more streams. Subscriber services then subscribe to those streams
without requiring any knowledge of how a stream is published.

Publish-subscribe overview
==========================
Applications can publish streams to a topic name which can then be
subscribed to by other applications (or even the same application).
Publish-subscribe works across applications written in SPL and those written
using the Java/Scala and Python application APIs.

A subscriber matches a publisher if their topic filter matches a
publisher's topic name and the stream type (schema) is an exact match to that
of the publisher. It is recommended that a single stream type is used
for a topic name.

A topic is a string value (encoded with UTF-8), based upon the
`MQTT topic style <http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#appendix-a>`_

Topic names for publishing a stream:

* Must be at least one character long.
* Use `/` as a level separator, zero length topic levels are valid.
* Must not include wild card characters `\+` and `\#`.
* Must not include the Unicode character NULL (U+0000).

Topic filters for subscribing to streams:

* Must be at least one character long.
* Use `/` as a level separator.
* Must not include the Unicode character NULL (U+0000).
* `\+` is a single-level wildcard character that can be used at any level, but it must occupy the entire level. `+`, `a/b/+`, `+/b/+` and `+/b` are valid but `a/b/c+` is not valid.
* `\#` is a wildcard character that matches any number of levels including the parent and any number of child levels. The multi-level wildcard character must be specified either on its own or following a topic level separator. In either case it must be the last character specified in the topic filter. `#` and 'a/b/#' are valid but `a/b/c#` and `a/#/c` are not valid.

Without a wildcard character a topic filter is an exact match for a topic name
so that filter `a/b/c` only matches `a/b/c`.

Single-level filter (`+`) match examples are:

* filter `+` matches `a` and `b` but not `a/b`
* filter `a/+` matches `a/b`, `a/c` and `a/` but not `a`, `b/c` or `a/b/c`
* filter `+/+` matches `a/b`, `b/c`, `d/` and `/`  but not `a` or `a/b/c`

Multi-level filter (`#`) match examples are:

* filter `#` matches every topic name such as `a`, `b/c`, `//`
* filter `a/b/#` matches `a/b` (parent), `a/b/c`, `a/b/d` and `a/b/c/d`
 
.. note::

    A publish-subscribe match requires the stream type
    to match as well as the topic filter matching the topic name.
  
Publish-subscribe is a many to many relationship,
any number of publishers can publish to the same topic
and stream type, and there can be many subscribers to a topic.

For example a telco ingest microservice/application may process
Call Detail Records from network switches and publish processed
records on multiple topics, ``cdr/voice/normal``,
``cdr/voice/dropped``, ``cdr/sms``, etc.
by publishing each processed stream with its own topic.
Then a dropped call analytic microservice would subscribe to the
``cdr/voice/dropped`` topic.

Publish-subscribe is dynamic, using IBM Streams
dynamic connections, an application
can be submitted that subscribes to topics
published by other already running applications.
Once the new application has initialized, it will
start consuming tuples from published streams from existing applications.
And any stream the new application publishes will be subscribed to
by existing applications where the topic and stream type matches.

An application only receives tuples that are published while
it is connected, thus tuples are lost during a connection failure.

A Python application publishes streams using :py:meth:`~topology.Stream.publish`
and subscribes using :py:meth:`~topology.Topology.subscribe`.

A stream of :py:const:`Python tuples <streamsx.topology.schema.CommonSchema.Python>` can only be subscribed to by Python Streams applications. All other
types (schemas) can be subscribed to by any Streams application.

"""

import logging


_debug = logging.getLogger('streamsx.topology.internal')
_debug.addHandler(logging.NullHandler())
_debug.setLevel(logging.CRITICAL)
