# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2017
"""Context for submission of applications.

The main function is :py:func:`submit` to submit
a :py:class:`~streamsx.topology.topology.Topology`
to a Streaming Analytics service or IBM® Streams instance for execution.

"""
from __future__ import print_function
from __future__ import unicode_literals
from __future__ import division
from __future__ import absolute_import
try:
    from future import standard_library
    standard_library.install_aliases()
except (ImportError, NameError):
    # nothing to do here
    pass

from streamsx import rest
import logging
import os
import os.path
import json
import subprocess
import threading
import sys
import codecs
import tempfile

logger = logging.getLogger('streamsx.topology.context')

#
# Submission of a python graph using the Java Application API
# The JAA is reused to have a single set of code_createJSONFile that creates
# SPL, the toolkit, the bundle and submits it to the relevant
# environment
#
def submit(ctxtype, graph, config=None, username=None, password=None):
    """
    Submits a `Topology` (application) using the specified context type.

    Used to submit an application for compilation into a Streams application and
    execution within an Streaming Analytics service or IBM Streams instance.

    `ctxtype` defines how the application will be submitted, see :py:class:`ContextTypes`.

    The parameters `username` and `password` are only required when submitting to an
    IBM Streams instance and it is required to access the Streams REST API from the
    code performing the submit. Accessing data from views created by
    :py:meth:`~streamsx.topology.topology.Stream.view` requires access to the Streams REST API.

    Args:
        ctxtype(str): Type of context the application will be submitted to. A value from :py:class:`ContextTypes`.
        graph(Topology): The application topology to be submitted.
        config(dict): Configuration for the submission.
        username(str): Username for the Streams REST api.
        password(str): Password for `username`.

    Returns:
        SubmissionResult: Result of the submission. For details of what is contained see the :py:class:`ContextTypes`
        constant passed as `ctxtype`.
    """
    graph = graph.graph

    if not graph.operators:
        raise ValueError("Topology {0} does not contain any streams.".format(graph.topology.name))

    context_submitter = _SubmitContextFactory(graph, config, username, password).get_submit_context(ctxtype)
    return SubmissionResult(context_submitter.submit())



class _BaseSubmitter(object):
    """
    A submitter which handles submit operations common across all submitter types..
    """
    def __init__(self, ctxtype, config, graph):
        self.ctxtype = ctxtype
        self.config = dict()
        if config is not None:
            # Make copy of config to avoid modifying
            # the callers config
            self.config.update(config)
        self.graph = graph
        self.fn = None
        self.results_file = None

    def _config(self):
        "Return the submit configuration"
        return self.config

    def submit(self):
        # Convert the JobConfig into overlays
        self._create_job_config_overlays()

        # encode the relevant python version information into the config
        self._add_python_info()

        # Create the json file containing the representation of the application
        try:
            self._create_json_file(self._create_full_json())
        except IOError:
            logger.error("Error writing json graph to file.")
            raise

        tk_root = self._get_toolkit_root()

        cp = os.path.join(tk_root, "lib", "com.ibm.streamsx.topology.jar")

        streams_install = os.environ.get('STREAMS_INSTALL')
        # If there is no streams install, get java from JAVA_HOME and use the remote contexts.
        if streams_install is None:
            java_home = os.environ.get('JAVA_HOME')
            if java_home is None:
                raise ValueError("JAVA_HOME not found. Please set the JAVA_HOME system variable")

            jvm = os.path.join(java_home, "bin", "java")
            submit_class = "com.ibm.streamsx.topology.context.remote.RemoteContextSubmit"
        # Otherwise, use the Java version from the streams install
        else:
            jvm = os.path.join(streams_install, "java", "jre", "bin", "java")
            if ConfigParams.FORCE_REMOTE_BUILD in self.config and self.config[ConfigParams.FORCE_REMOTE_BUILD]:
                submit_class = "com.ibm.streamsx.topology.context.remote.RemoteContextSubmit"
            else:
                submit_class = "com.ibm.streamsx.topology.context.local.StreamsContextSubmit"
            cp = cp + ':' + os.path.join(streams_install, "lib", "com.ibm.streams.operator.samples.jar")

        args = [jvm, '-classpath', cp, submit_class, self.ctxtype, self.fn]
        logger.info("Generating SPL and submitting application.")
        proc_env = env=self._get_java_env()
        process = subprocess.Popen(args, stdin=None, stdout=subprocess.PIPE, stderr=subprocess.PIPE, bufsize=0, env=proc_env)

        stderr_thread = threading.Thread(target=_print_process_stderr, args=([process, self]))
        stderr_thread.daemon = True
        stderr_thread.start()

        stdout_thread = threading.Thread(target=_print_process_stdout, args=([process]))
        stdout_thread.daemon = True
        stdout_thread.start()
        process.wait()

        results_json = {}

        # Only try to read the results file if the submit was successful.
        if process.returncode == 0:
            with open(self.results_file) as _file:
                try:
                    results_json = json.loads(_file.read())
                except IOError:
                    logger.error("Could not read file:" + str(_file.name))
                    raise
                except json.JSONDecodeError:
                    logger.error("Could not parse results file:" + str(_file.name))
                    raise
                except:
                    logger.error("Unknown error while processing results file.")
                    raise

        _delete_json(self)
        results_json['return_code'] = process.returncode
        self._augment_submission_result(results_json)
        self.submission_results = results_json
        return results_json


    def _augment_submission_result(self, submission_result):
        """Allow a subclass to augment a submission result"""
        pass

    def _get_java_env(self):
        "Get the environment to be passed to the Java execution"
        return dict(os.environ)

    def _add_python_info(self):
        # Python information added to deployment
        pi = {}
        pi["prefix"] = sys.exec_prefix
        pi["version"] = sys.version
        self.config["python"] = pi

    def _create_job_config_overlays(self):
        if ConfigParams.JOB_CONFIG in self.config:
            jco = self.config[ConfigParams.JOB_CONFIG]
            del self.config[ConfigParams.JOB_CONFIG]
            jco._add_overlays(self.config)

    def _create_full_json(self):
        fj = dict()

        # Removing Streams Connection object because it is not JSON serializable, and not applicable for submission
        # Need to re-add it, since the StreamsConnection needs to be returned from the submit.
        sc = self.config.pop(ConfigParams.STREAMS_CONNECTION, None)
        fj["deploy"] = self.config.copy()
        fj["graph"] = self.graph.generateSPLGraph()

        _file = tempfile.NamedTemporaryFile(prefix="results", suffix=".json", mode="w+t", delete=False)
        _file.close()
        fj["submissionResultsFile"] = _file.name
        self.results_file = _file.name
        logger.debug("Results file created at " + _file.name)

        self.config[ConfigParams.STREAMS_CONNECTION] = sc
        return fj

    def _create_json_file(self, fj):
        if sys.hexversion < 0x03000000:
            tf = tempfile.NamedTemporaryFile(mode="w+t", suffix=".json", prefix="splpytmp", delete=False)
        else:
            tf = tempfile.NamedTemporaryFile(mode="w+t", suffix=".json", encoding="UTF-8", prefix="splpytmp",
                                         delete=False)
        tf.write(json.dumps(fj, sort_keys=True, indent=2, separators=(',', ': ')))
        tf.close()

        self.fn = tf.name

    def _setup_views(self):
        # Link each view back to this context.
        if self.graph.get_views():
            for view in self.graph.get_views():
                view._submit_context = self

    def streams_connection(self):
        raise NotImplementedError("Views require submission to DISTRIBUTED or ANALYTICS_SERVICE context")

    # There are two modes for execution.
    #
    # Pypi (Python focused)
    #  Pypi (pip install) package includes the SPL toolkit as
    #      streamsx/.toolkit/com.ibm.streamsx.topology
    #      However the streamsx Python packages have been moved out
    #      of the toolkit's (opt/python/package) compared
    #      to the original toolkit layout. They are moved to the
    #      top level of the pypi package.
    #
    # SPL Toolkit (SPL focused):
    #   Streamsx Python packages are executed from opt/python/packages
    #
    # This function determines the root of the SPL toolkit based
    # upon the existance of the '.toolkit' directory.
    #
    @staticmethod
    def _get_toolkit_root():
        # Directory of this file (streamsx/topology)
        dir = os.path.dirname(os.path.abspath(__file__))

        # This is streamsx
        dir = os.path.dirname(dir)

        # See if .toolkit exists, if so executing from
        # a pip install
        tk_root = os.path.join(dir, '.toolkit', 'com.ibm.streamsx.topology')
        if os.path.isdir(tk_root):
            return tk_root

        # Else dir is tk/opt/python/packages/streamsx

        dir = os.path.dirname(dir)
        dir = os.path.dirname(dir)
        dir = os.path.dirname(dir)
        tk_root = os.path.dirname(dir)
        return tk_root


class _StreamingAnalyticsSubmitter(_BaseSubmitter):
    """
    A submitter supports the ANALYTICS_SERVICE (Streaming Analytics service) context.
    """
    def __init__(self, ctxtype, config, graph):
        super(_StreamingAnalyticsSubmitter, self).__init__(ctxtype, config, graph)
        self._streams_connection = self._config().get(ConfigParams.STREAMS_CONNECTION)
        self._vcap_services = self._config().get(ConfigParams.VCAP_SERVICES)
        self._service_name = self._config().get(ConfigParams.SERVICE_NAME)

        if self._streams_connection is not None:
            if not isinstance(self._streams_connection, rest.StreamingAnalyticsConnection):
                raise ValueError("config must contain a StreamingAnalyticsConnection object when submitting to "
                                 "{} context".format(ctxtype))

            # Use credentials stored within StreamingAnalyticsConnection
            self._service_name = self._streams_connection.service_name
            self._vcap_services = {'streaming-analytics': [
                {'name': self._service_name, 'credentials': self._streams_connection.credentials}
            ]}
            self._config()[ConfigParams.SERVICE_NAME] = self._service_name

            # TODO: Compare credentials between the config and StreamsConnection, verify they are the same

        # Clear the VCAP_SERVICES key in config, since env var will contain the content
        self._config().pop(ConfigParams.VCAP_SERVICES, None)

        self._setup_views()

    def streams_connection(self):
        if self._streams_connection is None:
            self._streams_connection = rest.StreamingAnalyticsConnection(self._vcap_services, self._service_name)
        return self._streams_connection

    def _augment_submission_result(self, submission_result):
        vcap = rest._get_vcap_services(self._vcap_services)
        credentials = rest._get_credentials(vcap, self._service_name)
        instance_id = credentials['jobs_path'].split('/service_instances/', 1)[1].split('/', 1)[0]
        submission_result['instanceId'] = instance_id
        submission_result['streamsConnection'] = self.streams_connection()

    def _get_java_env(self):
        "Pass the VCAP through the environment to the java submission"
        env = super(_StreamingAnalyticsSubmitter, self)._get_java_env()
        vcap = rest._get_vcap_services(self._vcap_services)
        env['VCAP_SERVICES'] = json.dumps(vcap)
        return env


class _DistributedSubmitter(_BaseSubmitter):
    """
    A submitter which supports the DISTRIBUTED (on-prem cluster) context.
    """
    def __init__(self, ctxtype, config, graph, username, password):
        _BaseSubmitter.__init__(self, ctxtype, config, graph)

        self._streams_connection = config.get(ConfigParams.STREAMS_CONNECTION)
        self.username = username
        self.password = password

        # Verify if credential (if supplied) is consistent with those in StreamsConnection
        if self._streams_connection is not None:
            self.username = self._streams_connection.rest_client._username
            self.password = self._streams_connection.rest_client._password
            if ((username is not None and username != self.username or
                 password is not None and password != self.password)):
                raise RuntimeError('Credentials supplied in the arguments differ than '
                                   'those specified in the StreamsConnection object')

        # Give each view in the app the necessary information to connect to SWS.
        self._setup_views()

    def streams_connection(self):
        if self._streams_connection is None:
            self._streams_connection = rest.StreamsConnection(self.username, self.password)
        return self._streams_connection

    def _augment_submission_result(self, submission_result):
        submission_result['instanceId'] = os.environ.get('STREAMS_INSTANCE_ID', 'StreamsInstance')
        # If we have the information to create a StreamsConnection, do it
        if not ((self.username is None or self.password is None) and
                        self.config.get(ConfigParams.STREAMS_CONNECTION) is None):
            submission_result['streamsConnection'] = self.streams_connection()


class _SubmitContextFactory(object):
    """
    ContextSubmitter:
        Responsible for performing the correct submission depending on a number of factors, including: the
        presence/absence of a streams install, the type of context, and whether the user seeks to retrieve data via rest
    """
    def __init__(self, graph, config=None, username=None, password=None):
        self.graph = graph
        self.config = config
        self.username = username
        self.password = password

        if self.config is None:
            self.config = {}

    def get_submit_context(self, ctxtype):

        # If there is no streams install present, currently only ANALYTICS_SERVICE, TOOLKIT, and BUILD_ARCHIVE
        # are supported.
        streams_install = os.environ.get('STREAMS_INSTALL')
        if streams_install is None:
            if not (ctxtype == ContextTypes.TOOLKIT or ctxtype == ContextTypes.BUILD_ARCHIVE
                    or ctxtype == ContextTypes.ANALYTICS_SERVICE or ctxtype == ContextTypes.STREAMING_ANALYTICS_SERVICE):
                raise ValueError(ctxtype + " must be submitted when an IBM Streams install is present.")

        if ctxtype == ContextTypes.DISTRIBUTED:
            logger.debug("Selecting the DISTRIBUTED context for submission")
            return _DistributedSubmitter(ctxtype, self.config, self.graph, self.username, self.password)
        elif ctxtype == ContextTypes.ANALYTICS_SERVICE or ctxtype == ContextTypes.STREAMING_ANALYTICS_SERVICE:
            logger.debug("Selecting the STREAMING_ANALYTICS_SERVICE context for submission")
            if not (sys.version_info.major == 3 and sys.version_info.minor == 5):
                raise RuntimeError("The ANALYTICS_SERVICE context only supports Python version 3.5")
            ctxtype = ContextTypes.STREAMING_ANALYTICS_SERVICE
            return _StreamingAnalyticsSubmitter(ctxtype, self.config, self.graph)
        else:
            logger.debug("Using the BaseSubmitter, and passing the context type through to java.")
            return _BaseSubmitter(ctxtype, self.config, self.graph)


# Used to delete the JSON file after it is no longer needed.
def _delete_json(submitter):
    for fn in [submitter.fn, submitter.results_file]:
        if os.path.isfile(fn):
            os.remove(fn)


# Used by a thread which polls a subprocess's stdout and writes it to stdout
def _print_process_stdout(process):
    try:
        while True:
            if sys.version_info.major == 2:
                sout = codecs.getwriter('utf8')(sys.stdout)
            line = process.stdout.readline()
            if len(line) == 0:
                process.stdout.close()
                break
            line = line.decode("utf-8").strip()
            if sys.version_info.major == 2:
                sout.write(line)
                sout.write("\n")
            else:
                print(line)
    except:
        logger.error("Error reading from Java subprocess stdout stream.")
        raise
    finally:
        process.stdout.close()


# Used by a thread which polls a subprocess's stderr and writes it to stderr, until the sc compilation
# has begun.
def _print_process_stderr(process, submitter):
    try:
        if sys.version_info.major == 2:
            serr = codecs.getwriter('utf8')(sys.stderr)
        while True:
            line = process.stderr.readline()
            if len(line) == 0:
                process.stderr.close()
                break
            line = line.decode("utf-8").strip()
            if sys.version_info.major == 2:
                serr.write(line)
                serr.write("\n")
            else:
                print(line)
    except:
        logger.error("Error reading from Java subprocess stderr stream.")
        raise
    finally:
        process.stderr.close()

class ContextTypes(object):
    """
        Submission context types.

        A :py:class:`~streamsx.topology.topology.Topology` is submitted using :py:func:`submit` and a context type.
        Submision of a `Topology` generally builds the application into a Streams application
        bundle (sab) file and then submits it for execution in the required context.

        The Streams application bundle contains all the artifacts required by an application such
        that it can be executed remotely (e.g. on a Streaming Analytics service), including
        distributing the execution of the application across multiple resources (hosts).

        The context type defines which context is used for submission.

        The main context types result in a running application and are:

            * :py:const:`STREAMING_ANALYTICS_SERVICE` - Application is submitted to a Streaming Analytics service running on IBM Bluemix cloud platform.
            * :py:const:`DISTRIBUTED` - Application is submitted to an IBM Streams instance.
            * :py:const:`STANDALONE` - Application is executed as a local process, IBM Streams `standalone` application. Typically this is used during development or testing.

        The :py:const:`BUNDLE` context type compiles the application (`Topology`) to produce a
        Streams application bundle (sab file). The bundle is not executed but may subsequently be submitted
        to a Streaming Analytics service or an IBM Streams instance. A bundle may be submitted multiple
        times to services or instances, each resulting in a unique job (running application).
    """
    STREAMING_ANALYTICS_SERVICE = 'STREAMING_ANALYTICS_SERVICE'
    """Submission to Streaming Analytics service running on IBM Bluemix cloud platform.

    The `Topology` is compiled and the resultant Streams application bundle
    (sab file) is submitted for execution on the Streaming Analytics service.

    When **STREAMS_INSTALL** is not set or the :py:func:`submit` `config` parameter has
    :py:const:`~ConfigParams.FORCE_REMOTE_BUILD` set to `True` the compilation of the application
    occurs remotely by the service. This allows creation and submission of Streams applications
    without a local install of IBM Streams.

    When **STREAMS_INSTALL** is set and the :py:func:`submit` `config` parameter has
    :py:const:`~ConfigParams.FORCE_REMOTE_BUILD` set to `False` or not set then the creation of the
    Streams application bundle occurs locally and the bundle is submitted for execution on the service.

    Environment variables:
        These environment variables define how the application is built and submitted.

        * **STREAMS_INSTALL** - (optional) Location of a IBM Streams installation (4.0.1 or later). The install must be running on RedHat/CentOS 6 and `x86_64` architecture.

    """
    ANALYTICS_SERVICE = 'ANALYTICS_SERVICE'
    """Synonym for :py:const:`STREAMING_ANALYTICS_SERVICE`.
    """
    DISTRIBUTED = 'DISTRIBUTED'
    """Submission to an IBM Streams instance.

    The `Topology` is compiled locally and the resultant Streams application bundle
    (sab file) is submitted to an IBM Streams instance.

    Environment variables:
        These environment variables define how the application is built and submitted.

        * **STREAMS_INSTALL** - Location of a IBM Streams installation (4.0.1 or later).
        * **STREAMS_DOMAIN_ID** - Domain identifier for the Streams instance.
        * **STREAMS_INSTANCE_ID** - Instance identifier.
        * **STREAMS_ZKCONNECT** - (optional) ZooKeeper connection string for domain (when not using an embedded ZooKeeper)

    """

    STANDALONE = 'STANDALONE'
    """Build and execute locally.

    Compiles and executes the `Topology` locally in IBM Streams standalone mode as a separate sub-process.
    Typically used for devlopment and testing.

    The call to :py:func:`submit` return when (if) the application completes. An application
    completes when it has finite source streams and all tuples from those streams have been
    processed by the complete topology. If the source streams are infinite (e.g. reading tweets)
    then the standalone application will not complete.

    Environment variables:
        This environment variables define how the application is built.

        * **STREAMS_INSTALL** - Location of a IBM Streams installation (4.0.1 or later).

    """

    BUNDLE = 'BUNDLE'
    """Create a Streams application bundle.

    The `Topology` is compiled locally to produce Streams application bundle (sab file).

    The resultant application can be submitted to:
        * Streaming Analytics service using the Streams console or the Streaming Analytics REST api.
        * IBM Streams instance using the Streams console, JMX api or command line ``streamtool submitjob``.
        * Executed standalone for development or testing (when built with IBM Streams 4.2 or later).

    The bundle must be built on the same operating system version and architecture as the intended running
    environment. For Streaming Analytics service this is currently RedHat/CentOS 6 and `x86_64` architecture.

    Environment variables:
        This environment variables define how the application is built.

        * **STREAMS_INSTALL** - Location of a IBM Streams installation (4.0.1 or later).

    """
    TOOLKIT = 'TOOLKIT'
    """Creates an SPL toolkit.

    `Topology` applications are translated to SPL applications before compilation into an Streams application
    bundle. This context type produces the intermediate SPL toolkit that is input to the SPL compiler for
    bundle creation.

    .. note::

        `TOOLKIT` is typically only used when diagnosing issues with bundle generation.
    """

    BUILD_ARCHIVE = 'BUILD_ARCHIVE'
    """Creates a build archive.

    This context type produces the intermediate code archive used for bundle creation.

    .. note::

        `BUILD_ARCHIVE` is typically only used when diagnosing issues with bundle generation.
    """

    STANDALONE_BUNDLE = 'STANDALONE_BUNDLE'
    """Create a Streams application bundle for standalone execution.

    The `Topology` is compiled locally to produce Streams standalone application bundle (sab file).

    The resultant application can be submitted to:
        * Executed standalone for development or testing.

    The bundle must be built on the same operating system version and architecture as the intended running
    environment. For Streaming Analytics service this is currently RedHat/CentOS 6 and `x86_64` architecture.

    Environment variables:
        This environment variables define how the application is built.

        * **STREAMS_INSTALL** - Location of a IBM Streams installation (4.0.1 or 4.1.x).

    .. deprecated:: IBM Streams 4.2
        Use :py:const:`BUNDLE` when compiling with IBM Streams 4.2 or later.
    """


class ConfigParams(object):
    """
    Configuration options which may be used as keys in :py:func:`submit` `config` parameter.
    """
    VCAP_SERVICES = 'topology.service.vcap'
    """Streaming Analytics service credentials in **VCAP_SERVICES** format.

    Provides the connection credentials when connecting to a Streaming Analytics service
    using context type :py:const:`~ContextTypes.STREAMING_ANALYTICS_SERVICE`.

    The key overrides the environment variable **VCAP_SERVICES**.

    The value can be:
        * Path to a local file containing a JSON representation of the VCAP services information.
        * Dictionary containing the VCAP services information.

    """
    SERVICE_NAME = 'topology.service.name'
    """Streaming Analytics service name.

    Selects the specific Streaming Analytics service from VCAP services information
    defined by the the environment variable **VCAP_SERVICES** or the key :py:const:`VCAP_SERVICES` in the `submit` config.
    """
    FORCE_REMOTE_BUILD = 'topology.forceRemoteBuild'
    """Force a remote build of the application.

    When submitting to :py:const:`STREAMING_ANALYTICS_SERVICE` a local build of the Streams application bundle
    will occur if the environment variable **STREAMS_INSTALL** is set. Setting this flag to `True` ignores the
    local Streams install and forces the build to occur remotely using the service.

    """
    JOB_CONFIG = 'topology.jobConfigOverlays'
    """
    Key for a :py:class:`JobConfig` object representing a job configuration for a submission.
    """
    STREAMS_CONNECTION = 'topology.streamsConnection'
    """
    Key for a :py:class:`StreamsConnection` object for connecting to a running IBM Streams instance.
    """

class JobConfig(object):
    """
    Job configuration.

    `JobConfig` allows configuration of job that will result from
    submission of a py:class:`Topology` (application).

    A `JobConfig` is set in the `config` dictionary passed to :py:func:`~streamsx.topology.context.submit`
    using the key :py:const:`~ConfigParams.JOB_CONFIG`. :py:meth:`~JobConfig.add` exists as a convenience
    method to add it to a submission configuration.

    Args:
        job_name(str): The name that is assigned to the job. A job name must be unique within a Streasm instance
            When set to `None` a system generated name is used.
        job_group(str): The job group to use to control permissions for the submitted job.
        preload(bool): Specifies whether to preload the job onto all resources in the instance, even if the job is
            not currently needed on each. Preloading the job can improve PE restart performance if the PEs are
            relocated to a new resource.
        data_directory(str): Specifies the location of the optional data directory. The data directory is a path
            within the cluster that is running the Streams instance.
        tracing: Specify the application trace level. See :py:attr:`tracing`

    Example::

        # Submit a job with the name NewsIngester
        cfg = {}
        job_config = JobConfig(job_name='NewsIngester')
        job_config.add(cfg)
        context.submit('ANALYTICS_SERVICE', topo, cfg)
    """
    def __init__(self, job_name=None, job_group=None, preload=False, data_directory=None, tracing=None):
        self.job_name = job_name
        self.job_group = job_group
        self.preload = preload
        self.data_directory = data_directory
        self.tracing = tracing
        self._pe_count = None

    @property
    def tracing(self):
        """
        Runtime application trace level.

        The runtime application trace level can be a string with value ``error``, ``warn``, ``info``,
        ``debug`` or ``trace``.

        In addition a level from Python ``logging`` module can be used in with ``CRITICAL`` and ``ERROR`` mapping
        to ``error``, ``WARNING`` to ``warn``, ``INFO`` to ``info`` and ``DEBUG`` to ``debug``.

        Setting tracing to `None` or ``logging.NOTSET`` will result in the job submission using the Streams instance
        application trace level.

        The value of ``tracing`` is the level as a string (``error``, ``warn``, ``info``, ``debug`` or ``trace``)
        or None.

        """
        return self._tracing

    @tracing.setter
    def tracing(self, level):
        if level is None:
            pass
        elif level in {'error', 'warn', 'info', 'debug', 'trace'}:
            pass
        elif level == logging.CRITICAL or level == logging.ERROR:
            level = 'error'
        elif level == logging.WARNING:
            level = 'warn'
        elif level == logging.INFO:
            level = 'info'
        elif level == logging.DEBUG:
            level = 'debug'
        elif level == logging.NOTSET:
            level = None
        else:
            raise ValueError("Tracing value {0} not supported.".format(level))

        self._tracing = level

    @property
    def target_pe_count(self):
        """Target processing element count.

         When submitted against a Streams instance `target_pe_count` provides
         a hint to the scheduler as to how to partition the topology
         across processing elements (processes) for the job execution. When a job
         contains multiple processing elements (PEs) then the Streams scheduler can
         distributed the PEs across the resources (hosts) running in the instance.

         When set to ``None`` (the default) no hint is supplied to the scheduler.
         The number of PEs in the submitted job will be determined by the scheduler.

         The value is only a target and may be ignored when the topology contains
         :py:meth:`~Stream.isolate` calls.

         .. note::
             Only supported in Streaming Analytics service and IBM Streams 4.2 or later.
        """
        if self._pe_count is None:
            return None
        return int(self._pe_count)

    @target_pe_count.setter
    def target_pe_count(self, count):
        if count is not None:
            count = int(count)
            if count < 1:
                raise ValueError("target_pe_count must be greater than 0.")
        self._pe_count = count

    def add(self, config):
        """
        Add this `JobConfig` into a submission configuration object.

        Args:
            config(dict): Submission configuration.

        Returns:
            dict: config.

        """
        config[ConfigParams.JOB_CONFIG] = self
        return config

    def _add_overlays(self, config):
        """
        Add this as a jobConfigOverlays JSON to config.
        """
        jco = {}
        config["jobConfigOverlays"] = [jco]
        jc = {}

        if self.job_name is not None:
            jc["jobName"] = self.job_name
        if self.job_group is not None:
            jc["jobGroup"] = self.job_group
        if self.data_directory is not None:
            jc["dataDirectory"] = self.data_directory
        if self.preload:
            jc['preloadApplicationBundles'] = True
        if self.tracing is not None:
            jc['tracing'] = self.tracing

        if jc:
            jco["jobConfig"] = jc

        if self.target_pe_count is not None and self.target_pe_count >= 1:
            deployment = {'fusionScheme' : 'manual', 'fusionTargetPeCount' : self.target_pe_count}
            jco["deploymentConfig"] = deployment


class SubmissionResult(object):
    """Passed back to the user after a call to submit.
    Allows the user to use dot notation to access dictionary elements."""
    def __init__(self, results):
        self.results = results


    @property
    def job(self):
        """If able, returns the job associated with the submitted build.
        If a username/password, StreamsConnection, or vcap file was not supplied,
        returns None.

        *NOTE*: The @property tag supersedes __getattr__. In other words, this job method is
        called before __getattr__(self, 'job') is called.
        """
        if 'streamsConnection' in self.results:
            sc = self.streamsConnection
            inst = sc.get_instance(self.instanceId)
            return inst.get_job(self.jobId)
        return None

    def __getattr__(self, key):
        if key in self.__getattribute__("results"):
            return self.results[key]
        return self.__getattribute__(key)

    def __setattr__(self, key, value):
        if "results" in self.__dict__:
            results = self.results
            results[key] = value
        else:
            super(SubmissionResult, self).__setattr__(key, value)

    def __getitem__(self, item):
        return self.__getattr__(item)

    def __setitem__(self, key, value):
        return self.__setattr__(key, value)

    def __delitem__(self, key):
        if key in self.__getattribute__("results"):
            del self.results[key]
            return
        self.__delattr__(key)

    def __contains__(self, item):
        return item in self.results
