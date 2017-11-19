# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2017
"""
Python APIs for use with IBM® Streaming Analytics service on
IBM Bluemix® cloud platform and on-premises IBM Streams.

Python Application API for Streams
----------------------------------
Module that allows the definition and execution of streaming
applications implemented in Python.
Applications use Python code to process tuples and tuples are Python objects.

SPL operators may also be invoked from Python applications to allow
use of existing IBM Streams toolkits.

See :py:mod:`~streamsx.topology`

Python functions as SPL operators
---------------------------------
A Python function or class can be simply turned into an SPL primitive operator
to allow tuple processing using Python in an SPL application.

SPL (Streams Processing Language) is a domain specific language for streaming
analytics supported by Streams.

See :py:mod:`streamsx.spl.spl`

Streams Python REST API
-----------------------
Module that allows interaction with an running Streams instance or
service through HTTPS REST APIs.

See :py:mod:`streamsx.rest`

Streams Python Setup
--------------------

Streaming Analytics service
+++++++++++++++++++++++++++

The service instance has Anaconda 4.1 installed with Python 3.5 as the
runtime environment and has **PYTHONHOME** application environment variable
pre-configured.

Any streaming applications using Python must use Python 3.5 when
submitted to the service instance.

IBM Streams on-premises
+++++++++++++++++++++++

For a distributed cluster running Streams Python 2.7 or Python 3.5 may
be used.

Anaconda may be used as the Python runtime, Anaconda has the advantage of
being pre-built and including a number of standard packages. Ananconda
installs may be downloaded at: https://www.continuum.io/downloads .

.. note::
    An Anaconda distribution for Python 2.7 or 3.5 must be used.

If building Python from source then it must be built to support embedding
of the runtime with shared libraries (``--enabled-shared`` option to `configure`).

The Streams application environment variable **PYTHONHOME** must be set
to the Python install path.

This is set using `streamtool` as::

    streamtool setproperty --application-ev PYTHONHOME=path_to_python_install

The application environment variable may also be set using the Streams
console. The `Instance Management` view has an
`Application Environment Variables` section. Expanding the details
for that section allows modification of the set of environment
variables available to Streams applications.

The Python install path must be accessible on every application host
that will execute Python code within a Streams application.

"""

from pkgutil import extend_path
__path__ = extend_path(__path__, __name__)
