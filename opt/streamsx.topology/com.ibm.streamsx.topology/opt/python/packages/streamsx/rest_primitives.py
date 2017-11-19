# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016,2017
import logging
import requests
import queue
import threading
import time
import json
import re
from pprint import pformat

import streamsx.topology.schema

logger = logging.getLogger('streamsx.rest')


def _exact_resource(json_rep, id=None):
    if id is not None:
        if not 'id' in json_rep:
            return False
        return id == json_rep['id']
    return True


def _matching_resource(json_rep, name=None):
    if name is not None:
        if not 'name' in json_rep:
            return False
        return re.match(name, json_rep['name'])
    return True


class _ResourceElement(object):
    """Stores JSON response from a REST call, and expose its properties as attributes.

    Attributes:
        json_rep(dict): The JSON representation of the resource, its properties can be accessed directly using dot
            notation on the object.
    """
    def __init__(self, json_rep, rest_client):
        """
        Args:
            json_rep(dict): The JSON response from a REST call.
            rest_client(_StreamsRestClient): The client used to make the REST call.
        """
        self.rest_client = rest_client
        self.rest_self = json_rep.get('self', None)
        self.json_rep = json_rep

    def __str__(self):
        return pformat(self.__dict__)

    # Override getattr to retrieve attribute from response JSON
    def __getattr__(self, key):
        if 'json_rep' in self.__dict__:
            json = self.__getattribute__('json_rep')
            if key in json:
                return json[key]
        # Fallback to default behaviour
        return self.__getattribute__(key)

    # Prevent setting one of the JSON attribute into the object
    def __setattr__(self, key, value):
        if 'json_rep' in self.__dict__:
            json = self.__getattribute__('json_rep')
            if key in json:
                raise AttributeError('"{0}" is an immutable attribute.'.format(key))
        super(_ResourceElement, self).__setattr__(key, value)

    def refresh(self):
        """Refresh the resource and update the attributes to reflect the latest status.
        """
        self.json_rep = self.rest_client.make_request(self.rest_self)

    def _get_elements(self, url, key, eclass, id=None, name=None):
        """Get elements matching `id` or `name`

        Args:
            url(str): url of children.
            key(str): key in the returned JSON.
            eclass(subclass type of :py:class:`_ResourceElement`): element class to create instances of.
            id(str, optional): only return resources whose `id` property matches the given `id`
            name(str, optional): only return resources whose `name` property matches the given `name`

        Returns:
            list(_ResourceElement): List of `eclass` instances

        Raises:
            ValueError: both `id` and `name` are specified together
        """
        if id is not None and name is not None:
            raise ValueError("id and name cannot specified together")

        json_elements = self.rest_client.make_request(url)[key]
        return [eclass(element, self.rest_client) for element in json_elements
                if _exact_resource(element, id) and _matching_resource(element, name)]

    def _get_element_by_id(self, url, key, eclass, id):
        """Get a single element matching an `id`

        Args:
            url(str): url of children.
            key(str): key in the returned JSON.
            eclass(subclass type of :py:class:`_ResourceElement`): element class to create instances of.
            id(str): return resources whose `id` property matches the given `id`

        Returns:
            _ResourceElement: Element of type `eclass` matching the given `id`

        Raises:
            ValueError: No resource matches given `id` or multiple resources matching given `id`
        """
        elements = self._get_elements(url, key, eclass, id=id)
        if not elements:
            raise ValueError("No resource matching: {0}".format(id))
        if len(elements) == 1:
            return elements[0]
        raise ValueError("Multiple resources matching: {0}".format(id))


class _StreamsRestClient(object):
    """Handles the session connection with the Streams REST API
    """
    def __init__(self, username, password):
        """
        Args:
            username(str): The username of an authorized Streams user.
            password(str): The password associated with the username.
        """
        # Create session to reuse TCP connection
        # https authentication
        self._username = username
        self._password = password

        self.session = requests.Session()
        self.session.auth = (username, password)

    def make_request(self, url):
        logger.debug('Beginning a REST request to: ' + url)
        return self.session.get(url).json()

    def __str__(self):
        return pformat(self.__dict__)


class _ViewDataFetcher(object):
    """A callable which, when invoked with a thread, begins fetching data from the
    supplied view and populates the `View.items` queue.
    """
    def __init__(self, view, tuple_getter):
        self.view = view
        self.tuple_getter = tuple_getter
        self.stop = threading.Event()
        self.items = queue.Queue()

        self._last_collection_time = -1
        self._last_collection_time_count = 0

    def __call__(self):
        while not self._stopped():
            _items = self._get_deduplicated_view_items() or []
            for itm in _items:
                self.items.put(itm)
            time.sleep(1)

    def _get_deduplicated_view_items(self):
        # Retrieve the view object
        data_name = self.view.attributes[0]['name']
        items = self.view.get_view_items()
        data = []

        # The number of already seen tuples to ignore on the last millisecond time boundary
        ignore_last_collection_time_count = self._last_collection_time_count

        for item in items:
            # Ignore tuples from milliseconds we've already seen
            if item.collectionTime < self._last_collection_time:
                continue
            elif item.collectionTime == self._last_collection_time:
                # Ignore tuples within the millisecond which we've already seen.
                if ignore_last_collection_time_count > 0:
                    ignore_last_collection_time_count -= 1
                    continue

                # If we haven't seen it, continue
                data.append(self.tuple_getter(item))
            else:
                data.append(self.tuple_getter(item))

        if len(items) > 0:
            # Record the current millisecond time boundary.
            _last_collection_time = items[-1].collectionTime
            _last_collection_time_count = 0
            backwards_counter = len(items) - 1
            while backwards_counter >= 0 and items[backwards_counter].collectionTime == _last_collection_time:
                _last_collection_time_count += 1
                backwards_counter -= 1

            self._last_collection_time = _last_collection_time
            self._last_collection_time_count = _last_collection_time_count

        return data

    def _stopped(self):
        return self.stop.isSet()


def _get_view_json_tuple(item):
    """
    Get a tuple from a view with a schema
    tuple<rstring jsonString>
    """
    return json.loads(item.data['jsonString'])


def _get_view_string_tuple(item):
    """
    Get a tuple from a view with a schema
    tuple<rstring string>
    """
    return str(item.data['string'])


def _get_view_dict_tuple(item):
    """Tuple from REST was in JSON which has already been
    converted to a dic.
    """
    return item.data


class View(_ResourceElement):
    """The View resource element provides access to information about a view that is associated with an active job, and
    exposes methods to retrieve data from the view's stream.

    Attributes:
        id (str): An unique identifier for the view.
        name (str): View name.
        description (str): Description of the view.
        resourceType (str): Identifies the REST resource type, which is *view*.
        activateOption (str): Indicate when the view starts buffering data.
        maximumTupleRate (int): Maximum Number of tuples at which the view collects per second.
        logicalOperatorName (str): The logical name of the operator that contains the output port on which the view is
            created.
        bufferCapacitySeconds (int): Buffer size measured in seconds.
        bufferCapacityTuples (int): Buffer size measured in number of tuples.
        bufferCapacityUnits (str): Indicates whether the buffer capacity for the view is determined by *seconds*,
            *tuples* or *unknown*.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> views = instances[0].get_views()
        >>> print (views[0].resourceType)
        view
    """
    def __init__(self, json_view, rest_client):
        super(View, self).__init__(json_view, rest_client)    
        tuple_fn = _get_view_dict_tuple
        if len(self.attributes) == 1:
            attr_type = self.attributes[0]['type']
            attr_name = self.attributes[0]['name']
            if 'rstring' == attr_type:
                if 'jsonString' == attr_name:
                    tuple_fn = _get_view_json_tuple
                elif 'string' == attr_name:
                    tuple_fn = _get_view_string_tuple
        self._data_fetcher = None
        self._tuple_fn = tuple_fn

    def get_domain(self):
        """Get the Streams domain for the instance that owns this view.

        Returns:
            Domain: Streams domain for the instance owning this view.
        """
        return Domain(self.rest_client.make_request(self.domain), self.rest_client)

    def get_instance(self):
        """Get the Streams instance that owns this view.

        Returns:
            Instance: Streams instance owning this view.
        """
        return Instance(self.rest_client.make_request(self.instance), self.rest_client)

    def get_job(self):
        """Get the Streams job that owns this view.

        Returns:
            Job: Streams Job owning this view.
        """
        return Job(self.rest_client.make_request(self.job), self.rest_client)

    def stop_data_fetch(self):
        """Stops the thread that fetches data from the Streams view server.
        """
        if self._data_fetcher is not None:
            self._data_fetcher.stop.set()
            self._data_fetcher = None

    def start_data_fetch(self):
        """Starts a thread that fetches data from the Streams view server.

        Returns:
            queue.Queue: Queue containing view data.
        """
        self.stop_data_fetch()
        self._data_fetcher = _ViewDataFetcher(self, self._tuple_fn)
        t = threading.Thread(target=self._data_fetcher)
        t.start()
        return self._data_fetcher.items

    def get_view_items(self):
        """Get a list of :py:class:`ViewItem` elements associated with this view.

        Returns:
            list(ViewItem): List of ViewItem(s) associated with this view.
        """
        view_items = [ViewItem(json_view_items, self.rest_client) for json_view_items
                      in self.rest_client.make_request(self.viewItems)['viewItems']]
        logger.debug("Retrieved " + str(len(view_items)) + " items from view " + self.name)
        return view_items


class ViewItem(_ResourceElement):
    """Represents the data of a tuple, its type, and the time when it was collected from the stream.

    Attributes:
        collectionTime (long): Epoch time when this viewItem is collected from the stream.
        data (dict): Content of this viewItem.
        resourceType (str): Identifies the REST resource type, which is *viewItem*.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> views = instances[0].get_views()
        >>> viewitems = views[0].get_view_items()
        >>> print (viewitems[0].resourceType)
        viewItem
    """
    pass


class Host(_ResourceElement):
    """The host element resource provides access to information about a host that is allocated to a domain as a
    resource for running Streams services and applications.

    Attributes:
        name (str): Configuration name for the IBM Streams resource.
        resourceType (str): Identifies the REST resource type, which is *host*.
        ipAddress (str): IP address for the IBM Streams resource.
        processorCount (int): Number of processors on the IBM Streams resource.
        restrictedTags (list(str)): Set of resource tags that processing elements (PEs) must have to run on the IBM
            Streams resource.
        services (list(dict)): Name and status of each domain service that is designated to run on the IBM Streams
            resource.
        status(str): Status of the IBM Streams resource.
        tag(list(str)): Names of each tag that is assigned to the IBM Streams resource.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> domains = sc.get_domains()
        >>> hosts = domains[0].get_hosts()
        >>> print (hosts[0].resourceType)
        host
    """
    pass


class Job(_ResourceElement):
    """The job element resource provides access to information about a submitted job within a specified instance.

    Attributes:
        id (str): job ID.
        name (str): Name of the job.
        resourceType (str): Identifies the REST resource type, which is *job*.
        health (str): Health indicator for the job. Some possible values for this property include *healthy*,
            *partiallyHealthy*, *partiallyUnhealthy*, *unhealthy*, and *unknown*.
        applicationName (str): Name of the streams processing application that this job is running.
        jobGroup (str): Identifies the job group to which this job belongs.
        startedBy (str): Identifies the user ID that started this job.
        status (str): Status of this job. Some possible values for this property include *canceling*, *running*,
            *canceled*, and *unknown*.
        submitTime (long): Epoch time when this job was submitted.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> jobs = instances[0].get_jobs()
        >>> print (jobs[0].health)
        healthy
    """
    def get_views(self, name=None):
        """Get the list of :py:class:`View` elements associated with this job.

        Args:
            name(str, optional): Returns view(s) matching `name`.  `name` can be a regular expression.  If `name`
            is not supplied, then all views associated with this instance are returned.

        Returns:
            list(View): List of views matching `name`.
        """
        return self._get_elements(self.views, 'views', View, name=name)

    def get_domain(self):
        """Get the Streams domain that owns this job.

        Returns:
            Domain: Streams domain that owns this job.
        """
        return Domain(self.rest_client.make_request(self.domain), self.rest_client)

    def get_instance(self):
        """Get the Streams instance that owns this job.

        Returns:
            Instance: Streams instance that owns this job.
        """
        return Instance(self.rest_client.make_request(self.instance), self.rest_client)

    def get_hosts(self):
        """Get the list of :py:class:`Host` elements associated with this job.

        Returns:
            list(Host): List of Host elements associated with this job.
        """
        return self._get_elements(self.hosts, 'hosts', Host)

    def get_operator_connections(self):
        """Get the list of :py:class:`OperatorConnection` elements associated with this job.

        Returns:
            list(OperatorConnection): List of OperatorConnection elements associated with this job.
        """
        return self._get_elements(self.operatorConnections, 'connections', OperatorConnection)

    def get_operators(self):
        """Get the list of :py:class:`Operator` elements associated with this job.

        Returns:
            list(Operator): List of Operator elements associated with this job.
        """
        return self._get_elements(self.operators, 'operators', Operator)

    def get_pes(self):
        """Get the list of :py:class:`PE` elements associated with this job.

        Returns:
            list(PE): List of PE elements associated with this job.
        """
        return self._get_elements(self.pes, 'pes', PE)

    def get_pe_connections(self):
        """Get the list of :py:class:`PEConnection` elements associated with this job.

        Returns:
            list(PEConnection): List of PEConnection elements associated with this job.
        """
        return self._get_elements(self.peConnections, 'connections', PEConnection)

    def get_resource_allocations(self):
        """Get the list of :py:class:`ResourceAllocation` elements associated with this job.

        Returns:
            list(ResourceAllocation): List of ResourceAllocation elements associated with this job.
        """
        return self._get_elements(self.resourceAllocations, 'resourceAllocations', ResourceAllocation)

    def cancel(self, force=False):
        """Cancel this job.

        Args:
            force (bool, optional): Forcefully cancel this job.

        Returns:
            bool: True if the job was cancelled, otherwise False if an error occurred.
        """
        if not self.rest_client._sc._analytics_service:
            import streamsx.st as st
            if st._has_local_install:
                if not st._cancel_job(self.id, force):
                    if force:
                        return False
                    return st._cancel_job(self.id, force=True)
                return True
        else:
            self.rest_client._sc.get_streaming_analytics().cancel_job(self.id)
            return True
        raise NotImplementedError('Job.cancel()')


class Operator(_ResourceElement):
    """The operator element resource provides access to information about a specific operator in a job.

    Attributes:
        name(str): Operator name.
        resourceType(str): Identifies the REST resource type, which is *operator*.
        operatorKind(str): SPL primitive operator type for this operator.
        indexWithinJob(int): Index of this operator within the job.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> operators = instances[0].get_operators()
        >>> print (operators[0].resourceType)
        operator
    """
    def get_metrics(self, name=None):
        """Get metrics for an operator.

        Args:
            name(str, optional): Only return metrics matching `name`, where `name` can be a regular expression.  If
                `name` is not supplied, then all metrics for this operator are returned.

        Returns:
             list(Metric): List of matching metrics.
        """
        return self._get_elements(self.metrics, 'metrics', Metric, name=name)


class OperatorConnection(_ResourceElement):
    """The operator connection element resource provides access to information about a connection between two operator
    ports.

    Attributes:
        id(str): Unique ID of this operator connection within the instance.
        resourceType(str): Identifies the REST resource type, which is *operator*.
        required (bool): Indicates whether the connection is required.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> operatorconnections = instances[0].get_operator_connections()
        >>> print (operatorconnections[0].resourceType)
        operatorConnection
    """
    pass


class OperatorOutputPort(_ResourceElement):
    """Operator output port resource provides access to information about an output port
    for a specific operator.

    Attributes:
        name(str): Name of this output port.
        resourceType(str): Identifies the REST resource type, which is *operatorOutputPort*.
        indexWithinOperator(int): Index of the output port within the operator.
        streamName(str): Name of the stream that is associated with this output port.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> exportedstreams = instances[0].get_exported_streams()
        >>> operatoroutputport = exportedstreams[0].get_operator_output_port()
        >>> print (operatoroutputport.resourceType)
        operatorOutputPort
    """
    pass


class Metric(_ResourceElement):
    """Metric resource provides access to information about a Streams metric.

    Attributes:
        name(str): Name of this metric.
        resourceType(str): Identifies the REST resource type, which is *metric*.
        description(str): Describes this metric.
        lastTimeRetrieved(str): Epoch time when the metric was most recently retrieved.
        metricKind(str): Kind of metric. Some possible values include *counter*, *gauge*, *time* and *unknown*.
        metricType(str): Type of metric. Some possible values include *system*, *custom* and *unknown*.
        value(int): Value for the metric.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> operators = instances[0].get_operators()
        >>> metrics = operators[0].get_metrics()
        >>> print (metrics[0].resourceType)
        metric
    """
    pass


class PE(_ResourceElement):
    """The processing element (PE) resource provides access to information about a PE.

    Attributes:
        id(str): PE ID.
        resourceType(str): Identifies the REST resource type, which is *pe*.
        health(str): Health indicator for this PE. Some possible values include *healthy*, *partiallyHealthy*,
            *partiallyUnhealthy*, *unhealthy*, and *unknown*.
        indexWithinJob(int): Index of the PE within the job.
        launchCount(int): Number of times this PE was started manually or automatically because of failures.
        optionalConnections(str): Status of optional connections for this PE. Some possible values include *connected*,
            *disconnected*, *partiallyConnected*, and *unknown*.
        pendingTracingLevel(str): Describes a pending change to the granularity of the trace information that is
            stored for this PE. Some possible values include *off*, *error*, *debug* and *trace*.  The value is *None*,
            if no change is pending.
        processId(str): Operating system process ID for this PE.
        relocatable(bool): Indicates whether this PE can be relocated to a different resource.
        requiredConnections(str): Status of the required connections for this PE. Some possible values include
            *connected*, *disconnected*, *partiallyConnected*, and *unknown*.
        restartable(bool): Indicates whether this PE can be restarted.
        status(str): Status of this PE.
        statusReason(str): Additional information for the status of this PE.
        tracingLevel(str): Granularity of the trace information. Some possible values include *off*, *error*, *debug*
            and *trace*.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> pes = instances.get_pes()
        >>> print(pes[0].resourceType)
        pe
    """
    pass


class PEConnection(_ResourceElement):
    """The processing element (PE) connection resource provides access to information about a connection between two
    processing element (PE) ports.

    Attributes:
        id(str): PE connection ID.
        resourceType(str): Identifies the REST resource type, which is *peConnection*.
        required(bool): Indicates whether this connection is required.
        status(str): Status of this connection. Some possible values include *connected*, *disconnected*, and
            *unknown*.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> peconnections = instances.get_pe_connections()
        >>> print(peconnections[0].resourceType)
        peConnection
    """
    pass


class ResourceAllocation(_ResourceElement):
    """The ResourceAllocation element resource provides access to information about a resource that is allocated to
    an IBM Streams instance.

    Attributes:
        resourceType(str): Identifies the REST resource type, which is *resourceAllocation*.
        applicationResource(bool): Indicates whether this resource is an application resource, which is used to run
            streams processing applications.
        schedulerStatus(str): Indicates whether this resource is schedulable for the instance.
        status(str): Status of this resource for the instance.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> allocations = instances.get_resource_allocations()
        >>> print(allocations[0].resourceType)
        resourceAllocation
    """
    pass


class ActiveService(_ResourceElement):
    """The ActiveService element resource provides access to information about a domain or an instance service.

    Attributes:
        resourceType(str): Identifies the REST resource type, which is *activeService*.
        leader(bool): If *True*, this service is a standby service.
        processId(str): Process ID of this service.
        startTime(long): Epoch time when this service started.
        status(str): Status of this service. Some possible values include *stopped*, *running*, *failed*, and
            *unknown*.
        type(str): Type of this service.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> services = instances.get_active_services()
        >>> print(services[0].resourceType)
        activeService
    """
    pass


class Installation(_ResourceElement):
    """The Installation element resource provides access to information about IBM Streams installations.

    Attributes:
        resourceType(str): Identifies the REST resource type, which is *installation*.
        architecture(str): Hardware architecture on which product is installed.
        buildVersion(str): Product build ID.
        editionName(str): Product edition.
        fullProductVersion(str): Full product version, including any hot fix.
        minimumOSBaseVersion(str): Minimum operating system version requirement.
        minimumOSPatchVersion(str): Minimum operating system patch requirement.
        productName(str): Product name.
        productVersion(str): Product version.
    """
    pass


class ImportedStream(_ResourceElement):
    """Imported stream resource represents a stream that has been imported by a job.

    Attributes:
        resourceType(str): Identifies the REST resource type, which is *importedStream*.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> importedstreams = instances[0].get_imported_streams()
        >>> print (importedstreams[0].resourceType)
        importedStream
    """
    pass


class ExportedStream(_ResourceElement):
    """Exported stream resource represents a stream that has been exported by a job.

    Attributes:
        resourceType(str): Identifies the REST resource type, which is *exportedStream*.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> exportedstreams = instances[0].get_exported_streams()
        >>> print (exportedstreams[0].resourceType)
        exportedStream
    """
    def get_operator_output_port(self):
        """Get the output port of this exported stream.

        Returns:
            OperatorOutputPort: Output port of this exported stream.
        """
        return OperatorOutputPort(self.rest_client.make_request(self.operatorOutputPort), self.rest_client)

    def _as_published_topic(self):
        """This stream as a PublishedTopic if it is published otherwise None
        """

        oop = self.get_operator_output_port()
        if not hasattr(oop, 'export'):
            return

        export = oop.export
        if export['type'] != 'properties':
            return

        seen_export_type = False
        topic = None

        for p in export['properties']:
            if p['type'] != 'rstring':
                continue
            if p['name'] == '__spl_exportType':
                if p['values'] == ['"topic"']:
                    seen_export_type = True
                else:
                    return
            if p['name'] == '__spl_topic':
                topic = p['values'][0]

        if seen_export_type and topic is not None:
            schema = None
            if hasattr(oop, 'tupleAttributes'):
                ta_url = oop.tupleAttributes
                ta_resp = self.rest_client.make_request(ta_url)
                schema = streamsx.topology.schema.StreamSchema(ta_resp['splType'])
            return PublishedTopic(topic[1:-1], schema)
        return


class Instance(_ResourceElement):
    """The instance element resource provides access to information about a Streams instance.

    Attributes:
        id(str): Unique ID for this instance.
        resourceType(str): Identifies the REST resource type, which is *instance*.
        creationTime(long): Epoch time when this instance was created.
        creationuser(str): User ID that created this instance.
        health(str): Summarize status of the jobs in the instance. Some possible values include *healthy*,
            *partiallyHealthy*, *partiallyUnhealthy*, *unhealthy*, and *unknown*.
        owner(str): User ID that owns this instance.
        startTime(long): Epoch time when this instance was started.
        status(str): Status of this instance. Some possible values include *running*, *failed*, *stopped*, and
            *unknown*.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> instances = sc.get_instances()
        >>> print (instances[0].resourceType)
        instance
    """
    def get_operators(self):
        """Get the list of :py:class:`Operator` elements associated with this instance.

        Returns:
            list(Operator): List of Operator elements associated with this instance.
        """
        return self._get_elements(self.operators, 'operators', Operator)

    def get_operator_connections(self):
        """Get the list of :py:class:`OperatorConnection` elements associated with this instance.

        Returns:
            list(OperatorConnection): List of OperatorConnection elements associated with this instance.
        """
        return self._get_elements(self.operatorConnections, 'connections', OperatorConnection)

    def get_pes(self):
        """Get the list of :py:class:`PE` elements associated with this instance resource.

        Returns:
            list(PE): List of PE elements associated with this instance.
        """
        return self._get_elements(self.pes, 'pes', PE)

    def get_pe_connections(self):
        """Get the list of :py:class:`PEConnection` elements associated with this instance.

        Returns:
            list(PEConnection): List of PEConnection elements associated with this instance.
        """
        return self._get_elements(self.peConnections, 'connections', PEConnection)

    def get_views(self, name=None):
        """Get the list of :py:class:`View` elements associated with this instance.

        Args:
            name(str, optional): Returns view(s) matching `name`.  `name` can be a regular expression.  If `name`
            is not supplied, then all views associated with this instance are returned.

        Returns:
            list(View): List of views matching `name`.
        """
        return self._get_elements(self.views, 'views', View, name=name)

    def get_hosts(self):
        """Get the list of :py:class:`Host` element associated with this instance.

        Returns:
            list(Host): List of Host element associated with this instance.
        """
        return self._get_elements(self.hosts, 'hosts', Host)

    def get_domain(self):
        """Get the Streams domain that owns this instance.

        Returns:
            Domain: Streams domain owning this instance.
        """
        return Domain(self.rest_client.make_request(self.domain), self.rest_client)

    def get_jobs(self, name=None):
        """Retrieves jobs running in this instance.

        Args:
            name (str, optional): Only return jobs containing property **name** that matches `name`. `name` can be a
                regular expression. If `name` is not supplied, then all jobs are returned.

        Returns:
            list(Job): A list of jobs matching the given `name`.
        """
        return self._get_elements(self.jobs, 'jobs', Job, None, name)

    def get_job(self, id):
        """Retrieves a job matching the given `id`

        Args:
            id (str): Job `id` to match.

        Returns:
            Job: Job matching the given `id`

        Raises:
            ValueError: No resource matches given `id` or multiple resources matching given `id`
        """
        return self._get_element_by_id(self.jobs, 'jobs', Job, str(id))

    def get_imported_streams(self):
        """Get the list of :py:class:`ImportedStream` elements associated with this instance.

        Returns:
            list(ImportedStream): List of ImportedStream elements associated with this instance.
        """
        return self._get_elements(self.importedStreams, 'importedStreams', ImportedStream)

    def get_exported_streams(self):
        """Get the list of :py:class:`ExportedStream` elements associated with this instance.

        Returns:
            list(ExportedStream): List of ExportedStream elements associated with this instance.
        """
        return self._get_elements(self.exportedStreams, 'exportedStreams', ExportedStream)

    def get_active_services(self):
        """Get the list of :py:class:`ActiveService` elements associated with this instance.

        Returns:
            list(ActiveService): List of ActiveService elements associated with this instance.
        """
        return self._get_elements(self.activeServices, 'activeServices', ActiveService)

    def get_resource_allocations(self):
        """Get the list of :py:class:`ResourceAllocation` elements associated with this instance.

        Returns:
            list(ResourceAllocation): List of ResourceAllocation elements associated with this instance.
        """
        return self._get_elements(self.resourceAllocations, 'resourceAllocations', ResourceAllocation)

    def get_published_topics(self):
        """Get a list of published topics for this instance.

        Streams applications publish streams to a a topic that can be subscribed to by other
        applications. This allows a microservice approach where publishers
        and subscribers are independent of each other.

        A published stream has a topic and a schema. It is recommended that a
        topic is only associated with a single schema.

        Streams may be published and subscribed by applications regardless of the
        implementation language. For example a Python application can publish
        a stream of JSON tuples that are subscribed to by SPL and Java applications.

        Returns:
             list(PublishedTopic): List of currently published topics.
        """
        published_topics = []
        # A topic can be published multiple times
        # (typically with the same schema) but the
        # returned list only wants to contain a topic,schema
        # pair once. I.e. the list of topics being published is
        # being returned, not the list of streams.
        seen_topics = {}
        for es in self.get_exported_streams():
            pt = es._as_published_topic()
            if pt is not None:
                if pt.topic in seen_topics:
                    if pt.schema is None:
                        continue
                    if pt.schema in seen_topics[pt.topic]:
                        continue
                    seen_topics[pt.topic].append(pt.schema)
                else:
                    seen_topics[pt.topic] = [pt.schema]
                published_topics.append(pt)

        return published_topics


class ResourceTag(object):
    """Contains information for a tag that is defined in a Streams domain

    Attributes:
        definition_format_properties(bool): Indicates whether the resource definition consists of one or more
            properties.
        description(str): Tag description.
        name(str): Tag name.
        properties_definition(list(str)): Contains the properties of the resource definition. Only present if
            `definition_format_properties` is *True*.
        reserved(bool): If *True*, this tag is defined by IBM Streams, and cannot be modified.
    """
    def __init__(self, json_resource_tag):
        self.definition_format_properties = json_resource_tag['definitionFormatProperties']
        self.description = json_resource_tag['description']
        self.name = json_resource_tag['name']
        self.properties_definition = json_resource_tag['propertiesDefinition']
        self.reserved = json_resource_tag['reserved']

    def __str__(self):
        return pformat(self.__dict__)


class ActiveVersion(object):
    """Contains IBM Streams installation information

    Attributes:
        architecture(str): Hardware architecture on which product is installed.
        build_version(str): Product build ID.
        edition_name(str): Product edition.
        full_product_version(str): Full product version, including any hot fix.
        minimum_os_base_version(str): Minimum operating system version requirement.
        minimum_os_patch_version(str): Minimum operating system patch requirement.
        product_name(str): Product name.
        product_version(str): Product version.
    """
    def __init__(self, json_active_version):
        self.architecture = json_active_version['architecture']
        self.build_version = json_active_version['buildVersion']
        self.edition_name = json_active_version['editionName']
        self.full_product_version = json_active_version['fullProductVersion']
        self.minimum_os_base_version = json_active_version['minimumOSBaseVersion']
        self.minimum_os_patch_version = json_active_version['minimumOSPatchVersion']
        self.minimum_os_version = json_active_version['minimumOSVersion']
        self.product_name = json_active_version['productName']
        self.product_version = json_active_version['productVersion']

    def __str__(self):
        return pformat(self.__dict__)


class PublishedTopic(object):
    """Metadata for a published topic.

    Attributes:
        topic(str): Published topic
        schema(str): Schema of topic
    """
    def __init__(self, topic, schema):
        """
        Args:
            topic: Published topic.
            schema: Schema of topic.
        """
        self.topic = topic
        self.schema = schema

    def __repr__(self):
        return pformat(self.__dict__)


class Domain(_ResourceElement):
    """The domain element resource provides access to information about a Streams domain.

    Attributes:
        id(str): Unique ID for this domain.
        resourceType(str): Identifies the REST resource type, which is *domain*.
        creationTime(long): Epoch time when this domain was created.
        creationuser(str): User ID that created this domain.
        status(str): Status of this domain.  Some possible values include *running*, *stopping*, *stopped*,
            *starting*, *removing*, and *unknown*.

    Example:
        >>> from streamsx import rest
        >>> sc = rest.StreamingAnalyticsConnection()
        >>> domains = sc.get_domains()
        >>> print (domains[0].resourceType)
        domain
    """
    def get_instances(self):
        """Get the list of :py:class:`Instance` elements associated with this domain.

        Returns:
            list(Instance): List of Instance elements associated with this domain.
        """
        return self._get_elements(self.instances, 'instances', Instance)

    def get_hosts(self):
        """Get the list of :py:class:`Host` elements associated with this domain.

        Returns:
            list(Host): List of Host elements associated with this domain.
        """
        return self._get_elements(self.hosts, 'hosts', Host)

    def get_active_services(self):
        """Get the list of :py:class:`ActiveService` elements associated with this domain.

        Returns:
            list(ActiveService): List of ActiveService elements associated with this domain.
        """
        return self._get_elements(self.activeServices, 'activeServices', ActiveService)

    def get_resource_allocations(self):
        """Get the list of :py:class:`ResourceAllocation` elements associated with this domain.

        Returns:
            list(ResourceAllocation): List of ResourceAllocation elements associated with this domain.
        """
        return self._get_elements(self.resourceAllocations, 'resourceAllocations', ResourceAllocation)

    def get_resources(self):
        """Get the list of :py:class:`Resource` elements associated with this domain.

        Returns:
            list(Resource): List of Resource elements associated with this domain.
        """
        return self._get_elements(self.resources, 'resources', Resource)


class Resource(_ResourceElement):
    """The Streams resource element provides access to information about a Streams resource.

    Attributes:
        name(str): Resource name.
    """
    def get_resource(self):
        return self.rest_client.make_request(self.resource)


def get_view_obj(_view, rc):
    for domain in rc.get_domains():
        for instance in domain.get_instances():
            for view in instance.get_views():
                if view.name == _view.name:
                    return view
    return None


class StreamingAnalyticsService(object):
    """Streaming Analytics service running on IBM Bluemix cloud platform.
    """
    def __init__(self, rest_client, credentials):
        """
        Args:
            rest_client (_StreamsRestClient): The client used to make the REST call.
            credentials (str): credentials for accessing Streaming Analytics service.
        """
        self.rest_client = rest_client
        self._credentials = credentials

    def _get_url(self, req_name):
        return self._credentials['rest_url'] + self._credentials[req_name]

    def cancel_job(self, job_id=None, job_name=None):
        """Cancel a running job.

        Args:
            job_id (str, optional): Identifier of job to be canceled.
            job_name (str, optional): Name of job to be canceled.

        Returns:
            dict: JSON response for the job cancel operation.
        """
        payload = {}
        if job_name is not None:
            payload['job_name'] = job_name
        if job_id is not None:
            payload['job_id'] = job_id

        jobs_url = self._get_url('jobs_path')
        return self.rest_client.session.delete(jobs_url, params=payload).json()

    def start_instance(self):
        """Start the instance for this Streaming Analytics service.

        Returns:
            dict: JSON response for the instance start operation.
        """
        start_url = self._get_url('start_path')
        return self.rest_client.session.put(start_url, json={}).json()

    def stop_instance(self):
        """Stop the instance for this Streaming Analytics service.

        Returns:
            dict: JSON response for the instance stop operation.
        """
        stop_url = self._get_url('stop_path')
        return self.rest_client.session.put(stop_url, json={}).json()

    def get_instance_status(self):
        """Get the status the instance for this Streaming Analytics service.

        Returns:
            dict: JSON response for the instance status operation.
        """
        status_url = self._get_url('status_path')
        return self.rest_client.session.get(status_url).json()
