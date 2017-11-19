# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2017

"""Testing support for streaming applications.

Allows testing of a streaming application by creation conditions
on streams that are expected to become valid during the processing.
`Tester` is designed to be used with Python's `unittest` module.

A complete application may be tested or fragments of it, for example a sub-graph can be tested
in isolation that takes input data and scores it using a model.

Supports execution of the application on
:py:const:`~streamsx.topology.context.ContextTypes.STREAMING_ANALYTICS_SERVICE`,
:py:const:`~streamsx.topology.context.ContextTypes.DISTRIBUTED`
or :py:const:`~streamsx.topology.context.ContextTypes.STANDALONE`.

A :py:class:`Tester` instance is created and associated with the :py:class:`Topology` to be tested.
Conditions are then created against streams, such as a stream must receive 10 tuples using
:py:meth:`~Tester.tuple_count`.

Here is a simple example that tests a filter correctly only passes tuples with values greater than 5::

    import unittest
    from streamsx.topology.topology import Topology
    from streamsx.topology.tester import Tester

    class TestSimpleFilter(unittest.TestCase):

        def setUp(self):
            # Sets self.test_ctxtype and self.test_config
            Tester.setup_streaming_analytics(self)

        def test_filter(self):
            # Declare the application to be tested
            topology = Topology()
            s = topology.source([5, 7, 2, 4, 9, 3, 8])
            s = s.filter(lambda x : x > 5)

            # Create tester and assign conditions
            tester = Tester(topology)
            tester.contents(s, [7, 9, 8])

            # Submit the application for test
            # If it fails an AssertionError will be raised.
           tester.test(self.test_ctxtype, self.test_config)


A stream may have any number of conditions and any number of streams may be tested.

A py:meth:`~Tester.local_check` is supported where a method of the
unittest class is executed once the job becomes healthy. This performs
checks from the context of the Python unittest class, such as
checking external effects of the application or using the REST api to
monitor the application.

.. warning::
    Python 3.5 and Streaming Analytics service or IBM Streams 4.2 or later is required when using `Tester`.
"""

import streamsx.ec as ec
import streamsx.topology.context as stc
import os
import unittest
import logging
import collections
import threading
from streamsx.rest import StreamsConnection
from streamsx.rest import StreamingAnalyticsConnection
from streamsx.topology.context import ConfigParams
import time

import streamsx.topology.tester_runtime as sttrt

_logger = logging.getLogger('streamsx.topology.test')

class Tester(object):
    """Testing support for a Topology.

    Allows testing of a Topology by creating conditions against the contents
    of its streams.

    Conditions may be added to a topology at any time before submission.

    If a topology is submitted directly to a context then the graph
    is not modified. This allows testing code to be inserted while
    the topology is being built, but not acted upon unless the topology
    is submitted in test mode.

    If a topology is submitted through the test method then the topology
    may be modified to include operations to ensure the conditions are met.

    .. warning::
        For future compatibility applications under test should not include intended failures that cause
        a processing element to stop or restart. Thus, currently testing is against expected application behavior.

    Args:
        topology: Topology to be tested.
    """
    def __init__(self, topology):
        self.topology = topology
        topology.tester = self
        self._conditions = {}
        self.local_check = None

    @staticmethod
    def setup_standalone(test):
        """
        Set up a unittest.TestCase to run tests using IBM Streams standalone mode.

        Requires a local IBM Streams install define by the STREAMS_INSTALL
        environment variable. If STREAMS_INSTALL is not set, then the
        test is skipped.

        Two attributes are set in the test case:
         * test_ctxtype - Context type the test will be run in.
         * test_config- Test configuration.

        Args:
            test(unittest.TestCase): Test case to be set up to run tests using Tester

        Returns: None
        """
        if not 'STREAMS_INSTALL' in os.environ:
            raise unittest.SkipTest("Skipped due to no local IBM Streams install")
        test.test_ctxtype = stc.ContextTypes.STANDALONE
        test.test_config = {}

    @staticmethod
    def setup_distributed(test):
        """
        Set up a unittest.TestCase to run tests using IBM Streams distributed mode.

        Requires a local IBM Streams install define by the STREAMS_INSTALL
        environment variable. If STREAMS_INSTALL is not set then the
        test is skipped.

        The Streams instance to use is defined by the environment variables:
         * STREAMS_ZKCONNECT - Zookeeper connection string
         * STREAMS_DOMAIN_ID - Domain identifier
         * STREAMS_INSTANCE_ID - Instance identifier

        Two attributes are set in the test case:
         * test_ctxtype - Context type the test will be run in.
         * test_config - Test configuration.

        Args:
            test(unittest.TestCase): Test case to be set up to run tests using Tester

        Returns: None
        """
        if not 'STREAMS_INSTALL' in os.environ:
            raise unittest.SkipTest("Skipped due to no local IBM Streams install")

        if not 'STREAMS_INSTANCE_ID' in os.environ:
            raise unittest.SkipTest("Skipped due to STREAMS_INSTANCE_ID environment variable not set")
        if not 'STREAMS_DOMAIN_ID' in os.environ:
            raise unittest.SkipTest("Skipped due to STREAMS_DOMAIN_ID environment variable not set")

        test.username = os.getenv("STREAMS_USERNAME", "streamsadmin")
        test.password = os.getenv("STREAMS_PASSWORD", "passw0rd")

        test.test_ctxtype = stc.ContextTypes.DISTRIBUTED
        test.test_config = {}

    @staticmethod
    def setup_streaming_analytics(test, service_name=None, force_remote_build=False):
        """
        Set up a unittest.TestCase to run tests using Streaming Analytics service on IBM Bluemix cloud platform.

        The service to use is defined by:
         * VCAP_SERVICES environment variable containing `streaming_analytics` entries.
         * service_name which defaults to the value of STREAMING_ANALYTICS_SERVICE_NAME environment variable.

        If VCAP_SERVICES is not set or a service name is not defined, then the test is skipped.

        Two attributes are set in the test case:
         * test_ctxtype - Context type the test will be run in.
         * test_config - Test configuration.

        Args:
            test(unittest.TestCase): Test case to be set up to run tests using Tester
            service_name(str): Name of Streaming Analytics service to use. Must exist as an
                entry in the VCAP services. Defaults to value of STREAMING_ANALYTICS_SERVICE_NAME environment variable.

        Returns: None
        """
        if not 'VCAP_SERVICES' in os.environ:
            raise unittest.SkipTest("Skipped due to VCAP_SERVICES environment variable not set")

        test.test_ctxtype = stc.ContextTypes.STREAMING_ANALYTICS_SERVICE
        if service_name is None:
            service_name = os.environ.get('STREAMING_ANALYTICS_SERVICE_NAME', None)
        if service_name is None:
            raise unittest.SkipTest("Skipped due to no service name supplied")
        test.test_config = {'topology.service.name': service_name}
        if force_remote_build:
            test.test_config['topology.forceRemoteBuild'] = True

    def add_condition(self, stream, condition):
        """Add a condition to a stream.

        Conditions are normally added through :py:meth:`tuple_count`, :py:meth:`contents` or :py:meth:`tuple_check`.

        This allows an additional conditions that are implementations of :py:class:`Condition`.

        Args:
            stream(Stream): Stream to be tested.
            condition(Condition): Arbitrary condition.

        Returns:
            Stream: stream
        """
        self._conditions[condition.name] = (stream, condition)
        return stream

    def tuple_count(self, stream, count, exact=True):
        """Test that a stream contains a number of tuples.

        If `exact` is `True`, then condition becomes valid when `count`
        tuples are seen on `stream` during the test. Subsequently if additional
        tuples are seen on `stream` then the condition fails and can never
        become valid.

        If `exact` is `False`, then the condition becomes valid once `count`
        tuples are seen on `stream` and remains valid regardless of
        any additional tuples.

        Args:
            stream(Stream): Stream to be tested.
            count(int): Number of tuples expected.
            exact(bool): `True` if the stream must contain exactly `count`
                tuples, `False` if the stream must contain at least `count` tuples.

        Returns:
            Stream: stream
        """
        _logger.debug("Adding tuple count (%d) condition to stream %s.", count, stream)
        if exact:
            name = "ExactCount" + str(len(self._conditions))
            cond = sttrt._TupleExactCount(count, name)
            cond._desc = "{0} stream expects tuple count equal to {1}.".format(stream.name, count)
        else:
            name = "AtLeastCount" + str(len(self._conditions))
            cond = sttrt._TupleAtLeastCount(count, name)
            cond._desc = "'{0}' stream expects tuple count of at least {1}.".format(stream.name, count)
        return self.add_condition(stream, cond)

    def contents(self, stream, expected, ordered=True):
        """Test that a stream contains the expected tuples.

        Args:
            stream(Stream): Stream to be tested.
            expected(list): Sequence of expected tuples.
            ordered(bool): True if the ordering of received tuples must match expected.

        Returns:
            Stream: stream
        """
        name = "StreamContents" + str(len(self._conditions))
        if ordered:
            cond = sttrt._StreamContents(expected, name)
            cond._desc = "'{0}' stream expects tuple ordered contents: {1}.".format(stream.name, expected)
        else:
            cond = sttrt._UnorderedStreamContents(expected, name)
            cond._desc = "'{0}' stream expects tuple unordered contents: {1}.".format(stream.name, expected)
        return self.add_condition(stream, cond)

    def tuple_check(self, stream, checker):
        """Check each tuple on a stream.

        For each tuple ``t`` on `stream` ``checker(t)`` is called.

        If the return evaluates to `False` then the condition fails.
        Once the condition fails it can never become valid.
        Otherwise the condition becomes or remains valid. The first
        tuple on the stream makes the condition valid if the checker
        callable evaluates to `True`.

        The condition can be combined with :py:meth:`tuple_count` with
        ``exact=False`` to test a stream map or filter with random input data.

        An example of combining `tuple_count` and `tuple_check` to test a filter followed
        by a map is working correctly across a random set of values::

            def rands():
                r = random.Random()
                while True:
                    yield r.random()

            class TestFilterMap(unittest.testCase):
            # Set up omitted

                def test_filter(self):
                    # Declare the application to be tested
                    topology = Topology()
                    r = topology.source(rands())
                    r = r.filter(lambda x : x > 0.7)
                    r = r.map(lambda x : x + 0.2)

                    # Create tester and assign conditions
                    tester = Tester(topology)
                    # Ensure at least 1000 tuples pass through the filter.
                    tester.tuple_count(r, 1000, exact=False)
                    tester.tuple_check(r, lambda x : x > 0.9)


                    # Submit the application for test
                    # If it fails an AssertionError will be raised.
                    tester.test(self.test_ctxtype, self.test_config)

        Args:
            stream(Stream): Stream to be tested.
            checker(callable): Callable that must evaluate to True for each tuple.

        """
        name = "TupleCheck" + str(len(self._conditions))
        cond = sttrt._TupleCheck(checker, name)
        return self.add_condition(stream, cond)

    def local_check(self, callable):
        """Perform local check while the application is being tested.

        A call to `callable` is made after the application under test is submitted and becomes healthy.
        The check is in the context of the Python runtime executing the unittest case,
        typically the callable is a method of the test case.

        The application remains running until all the conditions are met
        and `callable` returns. If `callable` raises an error, typically
        through an assertion method from `unittest` then the test will fail.

        Used for testing side effects of the application, typically with `STREAMING_ANALYTICS_SERVICE`
        or `DISTRIBUTED`. The callable may also use the REST api for context types that support
        it to dynamically monitor the running application.

        The callable can use `submission_result` and `streams_connection` attributes from :py:class:`Tester` instance
        to interact with the job or the running Streams instance.

        Simple example of checking the job is healthy::

            import unittest
            from streamsx.topology.topology import Topology
            from streamsx.topology.tester import Tester

            class TestLocalCheckExample(unittest.TestCase):
                def setUp(self):
                    Tester.setup_distributed(self)

                def test_job_is_healthy(self):
                    topology = Topology()
                    s = topology.source(['Hello', 'World'])

                    self.tester = Tester(topology)
                    self.tester.tuple_count(s, 2)

                    # Add the local check
                    self.tester.local_check = self.local_checks

                    # Run the test
                    self.tester.test(self.test_ctxtype, self.test_config)


                def local_checks(self):
                    job = self.tester.submission_result.job
                    self.assertEqual('healthy', job.health)

        .. warning::
            A local check must not cancel the job (application under test).

        Args:
            callable: Callable object.

        """
        self.local_check = callable

    def test(self, ctxtype, config=None, assert_on_fail=True, username=None, password=None):
        """Test the topology.

        Submits the topology for testing and verifies the test conditions are met and the job remained healthy through its execution.

        The submitted application (job) is monitored for the test conditions and
        will be canceled when all the conditions are valid or at least one failed.
        In addition if a local check was specified using :py:meth:`local_check` then
        that callable must complete before the job is cancelled.

        The test passes if all conditions became valid and the local check callable (if present) completed without
        raising an error.

        The test fails if the job is unhealthy, any condition fails or the local check callable (if present) raised an exception.

        Args:
            ctxtype(str): Context type for submission.
            config: Configuration for submission.
            assert_on_fail(bool): True to raise an assertion if the test fails, False to return the passed status.
            username(str): username for distributed tests
            password(str): password for distributed tests

        Attributes:
            submission_result: Result of the application submission from :py:func:`~streamsx.topology.context.submit`.
            streams_connection(StreamsConnection): Connection object that can be used to interact with the REST API of
                the Streaming Analytics service or instance.

        Returns:
            bool: `True` if test passed, `False` if test failed if `assert_on_fail` is `False`.

        """

        # Add the conditions into the graph as sink operators
        _logger.debug("Adding conditions to topology %s.", self.topology.name)
        for ct in self._conditions.values():
            condition = ct[1]
            stream = ct[0]
            stream.for_each(condition, name=condition.name)

        if config is None:
            config = {}
        _logger.debug("Starting test topology %s context %s.", self.topology.name, ctxtype)

        if stc.ContextTypes.STANDALONE == ctxtype:
            passed = self._standalone_test(config)
        elif stc.ContextTypes.DISTRIBUTED == ctxtype:
            passed = self._distributed_test(config, username, password)
        elif stc.ContextTypes.STREAMING_ANALYTICS_SERVICE == ctxtype or stc.ContextTypes.ANALYTICS_SERVICE == ctxtype:
            passed = self._streaming_analytics_test(ctxtype, config)
        else:
            raise NotImplementedError("Tester context type not implemented:", ctxtype)

        if 'conditions' in self.result:
            for cn,cnr in self.result['conditions'].items():
                c = self._conditions[cn][1]
                cdesc = cn
                if hasattr(c, '_desc'):
                    cdesc = c._desc

                if 'Fail' == cnr:
                    _logger.error("Condition: %s : %s", cnr, cdesc)
                elif 'NotValid' == cnr:
                    _logger.warning("Condition: %s : %s", cnr, cdesc)
                elif 'Valid' == cnr:
                    _logger.info("Condition: %s : %s", cnr, cdesc)
        
        if assert_on_fail:
            assert passed, "Test failed for topology: " + self.topology.name
        if passed:
            _logger.info("Test topology %s passed for context:%s", self.topology.name, ctxtype)
        else:
            _logger.error("Test topology %s failed for context:%s", self.topology.name, ctxtype)
        return passed

    def _standalone_test(self, config):
        """ Test using STANDALONE.
        Success is solely indicated by the process completing and returning zero.
        """
        sr = stc.submit(stc.ContextTypes.STANDALONE, self.topology, config)
        self.submission_result = sr
        self.result = {'passed': sr['return_code'], 'submission_result': sr}
        return sr['return_code'] == 0

    def _distributed_test(self, config, username, password):
        self.streams_connection = config.get(ConfigParams.STREAMS_CONNECTION)
        if self.streams_connection is None:
            # Supply a default StreamsConnection object with SSL verification disabled, because the default
            # streams server is not shipped with a valid SSL certificate
            self.streams_connection = StreamsConnection(username, password)
            self.streams_connection.session.verify = False
            config[ConfigParams.STREAMS_CONNECTION] = self.streams_connection
        sjr = stc.submit(stc.ContextTypes.DISTRIBUTED, self.topology, config)
        self.submission_result = sjr
        if sjr['return_code'] != 0:
            _logger.error("Failed to submit job to distributed instance.")
            return False
        return self._distributed_wait_for_result()

    def _streaming_analytics_test(self, ctxtype, config):
        sjr = stc.submit(ctxtype, self.topology, config)
        self.submission_result = sjr
        self.streams_connection = config.get(ConfigParams.STREAMS_CONNECTION)
        if self.streams_connection is None:
            vcap_services = config.get(ConfigParams.VCAP_SERVICES)
            service_name = config.get(ConfigParams.SERVICE_NAME)
            self.streams_connection = StreamingAnalyticsConnection(vcap_services, service_name)
        if sjr['return_code'] != 0:
            _logger.error("Failed to submit job to Streaming Analytics instance")
            return False
        return self._distributed_wait_for_result()

    def _distributed_wait_for_result(self):

        cc = _ConditionChecker(self, self.streams_connection, self.submission_result)
        # Wait for the job to be healthy before calling the local check.
        if cc._wait_for_healthy():
            self._start_local_check()
            self.result = cc._complete()
            if self.local_check is not None:
                self._local_thread.join()
        else:
            self.result = cc._end(False, _ConditionChecker._UNHEALTHY)

        self.result['submission_result'] = self.submission_result
        cc._canceljob(self.result)
        if self.local_check_exception is not None:
            raise self.local_check_exception
        return self.result['passed']

    def _start_local_check(self):
        self.local_check_exception = None
        if self.local_check is None:
            return
        self._local_thread = threading.Thread(target=self._call_local_check)
        self._local_thread.start()

    def _call_local_check(self):
        try:
            self.local_check_value = self.local_check()
        except Exception as e:
            self.local_check_value = None
            self.local_check_exception = e

#######################################
# Internal functions
#######################################

def _result_to_dict(passed, t):
    result = {}
    result['passed'] = passed
    result['valid'] = t[0]
    result['fail'] = t[1]
    result['progress'] = t[2]
    result['conditions'] = t[3]
    return result

class _ConditionChecker(object):
    _UNHEALTHY = (False, False, False, None)

    def __init__(self, tester, sc, sjr):
        self.tester = tester
        self._sc = sc
        self._sjr = sjr
        self._instance_id = sjr['instanceId']
        self._job_id = sjr['jobId']
        self._sequences = {}
        for cn in tester._conditions:
            self._sequences[cn] = -1
        self.delay = 0.5
        self.timeout = 10.0
        self.waits = 0
        self.additional_checks = 2

        self.job = self._find_job()

    # Wait for job to be healthy. Returns True
    # if the job became healthy, False if not.
    def _wait_for_healthy(self):
        while (self.waits * self.delay) < self.timeout:
            if self.__check_job_health():
                self.waits = 0
                return True
            time.sleep(self.delay)
            self.waits += 1
        return False

    def _complete(self):
        while (self.waits * self.delay) < self.timeout:
            check = self. __check_once()
            if check[1]:
                return self._end(False, check)
            if check[0]:
                if self.additional_checks == 0:
                    return self._end(True, check)
                self.additional_checks -= 1
                continue
            if check[2]:
                self.waits = 0
            else:
                self.waits += 1
            time.sleep(self.delay)
        return self._end(False, check)

    def _end(self, passed, check):
        result = _result_to_dict(passed, check)
        return result

    def _canceljob(self, result):
        if self.job is not None:
            self.job.cancel(force=not result['passed'])

    def __check_once(self):
        if not self.__check_job_health():
            return _ConditionChecker._UNHEALTHY
        cms = self._get_job_metrics()
        valid = True
        progress = True
        fail = False
        condition_states = {}
        for cn in self._sequences:
            condition_states[cn] = 'NotValid'
            seq_mn = sttrt.Condition._mn('seq', cn)
            # If the metrics are missing then the operator
            # is probably still starting up, cannot be valid.
            if not seq_mn in cms:
                valid = False
                continue
            seq_m = cms[seq_mn]
            if seq_m.value == self._sequences[cn]:
                progress = False
            else:
                self._sequences[cn] = seq_m.value

            fail_mn = sttrt.Condition._mn('fail', cn)
            if not fail_mn in cms:
                valid = False
                continue

            fail_m = cms[fail_mn]
            if fail_m.value != 0:
                fail = True
                condition_states[cn] = 'Fail'
                continue

            valid_mn =  sttrt.Condition._mn('valid', cn)

            if not valid_mn in cms:
                valid = False
                continue
            valid_m = cms[valid_mn]

            if valid_m.value == 0:
                valid = False
            else:
                condition_states[cn] = 'Valid'

        return (valid, fail, progress, condition_states)

    def __check_job_health(self):
        self.job.refresh()
        return self.job.health == 'healthy'

    def _find_job(self):
        instance = self._sc.get_instance(id=self._instance_id)
        return instance.get_job(id=self._job_id)

    def _get_job_metrics(self):
        """Fetch all the condition metrics for a job.
        We refetch the metrics each time to ensure that we don't miss
        any being added, e.g. if an operator is slow to start.
        """
        cms = {}
        for op in self.job.get_operators():
            metrics = op.get_metrics(name=sttrt.Condition._METRIC_PREFIX + '*')
            for m in metrics:
                cms[m.name] = m
        return cms
