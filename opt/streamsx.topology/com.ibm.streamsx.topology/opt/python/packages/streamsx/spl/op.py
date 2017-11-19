# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016,2017
"""
Invoking SPL Operators
++++++++++++++++++++++

IBM Streams supports *Stream Processing Language* (SPL),
a domain specific language for streaming analytics.
SPL creates an application by building a graph of operator
invocations. These operators are declared in a SPL toolkit.

SPL streams have a structured schema, such as
``tuple<rstring id, timestamp ts, float64 value>`` for
a sensor reading with a sensor identifier, timestamp and value.
A schema is defined using :py:class:`~streamsx.topology.schema.StreamSchema`.

A Python topology application can take advantage of SPL operators
by using streams with structured schemas. A stream of Python objects
can be converted to a structured stream using
:py:meth:`~streamsx.topology.topology.Stream.map`
with the `schema` parameter set::

    # s is stream of Python objects representing a sensor
    s = ...
    
    # map s to a structured stream using a lamda function
    # for each sensor reading r a Python tuple is created
    # with the required values matching the order of the
    # structured schema.
    s2 = s.map(lambda r : (r.sensor_id, r.reading_time, r.reading),
         schema='tuple<rstring id, timestamp ts, float64 value>'

An SPL operator is invoked in an application by creating an
instance of:

 * :py:class:`Invoke` - Invocation of an arbitrary SPL operator.
 * :py:class:`Source` - Invocation of an SPL source operator with one input port.
 * :py:class:`Map` - Invocation of an SPL map operator with one input port and one output port.
 * :py:class:`Sink` - Invocation of an SPL sink operator with one output port.

In SPL operator invocation support a number of clauses of which these are
supported from Python.

Param clause
------------
Operator parameterization is through operator parameters that configure
and modify the operator for the specific application.

Parameters are passed as a `dict` containing the parameter names and their values. A parameter value may be a constant, an input attribute or an
arbitrary SPL expression.

Example, a `Beacon` operator from the SPL standard toolkit producing 100 tuples at the rate of two per second::

    schema = StreamSchema('tuple<uint64 seq>')
    beacon = op.Source(topology, 'spl.utility::Beacon', schema,
        params = {'iterations':100, 'period':0.5})

SPL is strictly typed so when passing a constant as a parameter value the
value may need to be strongly typed. Python booleans, integers, floats and
strings map automatically to SPL `boolean`, `int32`, `float64` and `rstring`
respectively. The module :py:mod:`streamsx.spl.types` provides functions to
create typed SPL values.

For example to create a `count` parameter of type `uint64` for the SPL `DeDuplicate` operator::

    params['count'] = streamsx.spl.types.uint64(20)

After the instance representing the operator
invocation has been created, addition parameters may be added through
the `params` attribute. If the value is an expression that is only valid
in the context of the operator invocation then the parameter must be added
after 

For example, the `Filter` operator uses an expression that is usually dependent on the context, filtering tuples based upon their attribute values::

    fs = op.Map('spl.relational::Filter', beacon)
    fs.params['filter'] = fs.expression('seq % 2ul == 0ul')

Output clause
-------------

The operator output clause defines the values of attributes on outgoing
tuples on the operator invocation's output ports.

When a tuple is submitted by an operator invocation its attributes are
set one of three ways:

    * By the operator based upon its state and input tuples. For example a US ZIP code operator would set the `zipcode` attribute based upon its lookup of the ZIP code from the address details in the input tuple.
    * By the operator implicitly setting output attributes from matching input attributes. Many streaming operators implicitly set output attributes to allow attributes to flow through the operator without any explicit coding. This only occurs when an output attribute is not explicitly set by the operator or the output clause and the input tuple has an attribute that matches the name and type of the output attribute. For example in the US ZIP code operator if the output tuple included attributes of ``rstring city, rstring state`` matching input attributes then they would be implicitly copied from input tuple to output tuple.
    * By an ouput clause in the operator invocation. In this case the application invoking the operator is explicitly setting attributes using SPL expressions. An operator may provide output functions that return values based upon the operator's state and input tuples. For example the US ZIP code operator might provide a ``ZIPCode()`` output function rather than explicitly setting an output attribute. Then the application is free to use any attribute name to represent ZIP code in its output tuple.

In Python an output tuple attribute is set by creating an attribute in the operator invocation instance that is set to a return from the `output` method.

For example invoking a SPL `Beacon` operator using an output function to set the sequence number of a tuple and a SPL expression to set the timestamp::

    schema = StreamSchema('tuple<uint64 seq, timestamp ts>')
    beacon = op.Source(topology, 'spl.utility::Beacon', schema, params = {'period':0.1})

    # Set the seq attribute using an output function provided by Beacon
    beacon.seq = beacon.output('IterationCount()')

    # Set the ts attribute using an SPL function that returns the current time
    beacon.ts = beacon.output('getTimestamp()')

.. seealso::
    `Streams Processing Language (SPL) Reference <https://www.ibm.com/support/knowledgecenter/en/SSCRJU_4.2.0/com.ibm.streams.ref.doc/doc/spl-container.html>`_
         Reference documentation.
    `Developing Streams applications <https://www.ibm.com/support/knowledgecenter/en/SSCRJU_4.2.0/com.ibm.streams.dev.doc/doc/dev-container.html>`_
         Developing Streams applications.
    `Operator invocations <https://www.ibm.com/support/knowledgecenter/en/SSCRJU_4.2.0/com.ibm.streams.ref.doc/doc/operatorinvocations.html>`_
         Operator invocations from the SPL reference documentation.

"""

import streamsx.topology.exop as exop

class Invoke(exop.ExtensionOperator):
    """
    Declaration of an invocation of an SPL operator in a Topology.

    An SPL operator has an arbitrary of input ports and
    an arbitrary number of output ports. The kind of the
    operator places constraints on how many input and output
    ports it supports, and potentially the schemas for those
    ports. For example ``spl.relational::Filter`` has
    a single input port and one or two output ports,
    in addition the schemas of the ports must be identical.

    When the operator has output ports an instance of
    ``SPLOperator`` has an ``outputs`` attributes which
    is a list of ``Stream`` instances.

    Args:
        topology(Topology): Topology that will invoke the operator.
        kind(str): SPL operator kind, e.g. ``spl.utility::Beacon``.
        inputs: Streams to connect to the operator. If not set or set to
            `None` or an empty collection then the operator has no
            input ports. Otherwise a list or tuple of ``Stream`` instances
            where the number of items is the number of input ports.
        schemas: Schemas of the output ports. If not set or set to
            `None` or an empty collection then the operator has no
            outut ports. Otherwise a list or tuple of schemas
            where the number of items is the number of output ports.
        params: Operator parameters.
        name: Name of the operator. When `None` defaults to a name
            derived from the operator kind.
             
    """
    def __init__(self,topology,kind,inputs=None,schemas=None,params=None,name=None):
        action=None
        if name is None:
             if '::' in kind:
                 action = kind[kind.rfind('::') + 2 :]
             else:
                 action = kind
        name = topology.graph._requested_name(name, action)
        super(Invoke,self).__init__(topology,kind,inputs,schemas,params,name)
        self._op._ex_op = self

    def attribute(self, stream, name):
        """Expression for an input attribute.

        An input attribute is an attribute on one of the input
        ports of the operator invocation. `stream` must have been
        used to declare this invocation.

        Args:
            stream(Stream): Stream the attribute is from.
            name(str): Name of the attribute.
           
        Returns:
            Expression: Expression representing the input attribute.
        """
        if stream not in self._inputs:
            raise ValueError("Stream is not an input of this operator.")
        if len(self._inputs) == 1:
            return Expression('attribute', name)
        else:
            return Expression('attribute', stream.oport.name + '.' + name)

    def expression(self, value):
        """SPL expression.
 
        An arbitrary expression that is valid in the context of this operator.

        Args:
            value(str): Arbitrary SPL expression.

        Returns:
            Expression: Expression that is valid in the context of this operator.
        """
        return Expression.expression(value)

    def output(self, stream, value):
        """SPL output port assignment expression.

        Arguments:
            stream(Stream): Output stream the assignment is for.
            value(str): SPL expression used for an output assignment.

        Returns:
            Expression: Output assignment expression that is valid as a the context of this operator.
        """
        if stream not in self.outputs:
            raise ValueError("Stream is not an output of this operator.")
        e = self.expression(value)
        e._stream = stream
        return e

    def _generate(self, opjson):

        # For any attribute that is an expression
        # set it as an output clause assignment
        for attr in self.__dict__:
            e = self.__dict__[attr]
            if isinstance(e, Expression) and hasattr(e, '_stream'):
                opi = e._stream.oport.index
                port = opjson['outputs'][opi]
                if 'assigns' in port:
                    assigns = port['assigns']
                else:
                    assigns = {}
                    port['assigns'] = assigns

                assigns[attr] = e.spl_json()

class Source(Invoke):
    """
    Declaration of an invocation of an SPL *source* operator.

    Source operators typically bring external data into
    a Streams application as a stream. A source operator has
    no input ports and a single output port.

    An instance of Source has an attribute ``stream`` that is
    ``Stream`` produced by the operator.

    This is a utility class that allows simple invocation
    of the common case of a operator with a single output port.

    Args:
        topology(Topology): Topology that will invoke the operator.
        kind(str): SPL operator kind, e.g. ``spl.utility::Beacon``.
        schema: Schema of the output port.
        params: Operator parameters.
        name: Name of the operator. When `None` defaults to a generated name.
    """
    def __init__(self,topology,kind,schema,params=None,name=None):
        super(Source,self).__init__(topology, kind, schemas=schema, params=params,name=name)

    @property
    def stream(self):
        """
        Stream produced by the operator invocation.

        Returns:
            Stream: Stream produced by the operator invocation.
        """
        return self.outputs[0]

    def output(self, value):
        """SPL output port assignment expression.

        Arguments:
            value(str): SPL expression used for an output assignment.

        Returns:
            Expression: Output assignment expression that is valid as a the context of this operator.
        """
        return super(Source, self).output(self.stream, value)


class Map(Invoke):
    """
    Declaration of an invocation of an SPL *map* operator.

    *Map* operators have a single input port and single
    output port.

    An instance of Map has an attribute ``stream`` that is
    ``Stream`` produced by the operator.

    This is a utility class that allows simple invocation
    of the common case of a operator with a single input stream
    and single output stream.

    Args:
        kind(str): SPL operator kind, e.g. ``spl.relational::Filter``.
        stream: Stream to connect to the operator.
        schema: Schema of the output stream. If set to `None` then the output schema is the same as the schema of `stream`.
        params: Operator parameters.
        name: Name of the operator. When `None` defaults to a generated name.
    """
    def __init__(self,kind,stream,schema=None,params=None,name=None):
        if schema is None:
            schema = stream.oport.schema
        super(Map,self).__init__(stream.topology,kind,inputs=stream,schemas=schema,params=params,name=name)

    @property
    def stream(self):
        """
        Stream produced by the operator invocation.

        Returns:
            Stream: Stream produced by the operator invocation.
        """
        return self.outputs[0]

    def attribute(self, name):
        """Expression for an input attribute.

        An input attribute is an attribute on the input
        port of the operator invocation.

        Args:
            name(str): Name of the attribute.
           
        Returns:
            Expression: Expression representing the input attribute.
        """
        return super(Map, self).attribute(self._inputs[0], name)

    def output(self, value):
        """SPL output port assignment expression.

        Arguments:
            value(str): SPL expression used for an output assignment.

        Returns:
            Expression: Output assignment expression that is valid as a the context of this operator.
        """
        return super(Map, self).output(self.stream, value)

class Sink(Invoke):
    """
    Declaration of an invocation of an SPL sink operator.

    Source operators typically send data on a stream to an
    external system. A sink operator has a single input port
    and no output ports.

    This is a utility class that allows simple invocation
    of the common case of a operator with a single input port.

    Args:
        kind(str): SPL operator kind, e.g. ``spl.adapter::FileSink``.
        input: Stream to connect to the operator.
        params: Operator parameters.
        name: Name of the operator. When `None` defaults to a generated name.
    """
    def __init__(self,kind,stream,params=None,name=None):
        super(Sink,self).__init__(stream.topology,kind,inputs=stream,params=params,name=name)

class Expression(object):
    """An SPL expression.
    """
    def __init__(self, _type, _value):
        self._type = _type
        self._value = _value

    @staticmethod
    def expression(value):
        """Create an SPL expression.

        Args:
            value: Expression as a string or another `Expression`. If value is an instance of `Expression` then a new instance is returned containing the same type and value.

        Returns:
            Expression: SPL expression from `value`.
        """
        if isinstance(value, Expression):
            # Clone the expression to allow it to
            # be used in multiple contexts
            return Expression(value._type, value._value)
        return Expression('splexpr', value)

    def spl_json(self):
        """Private method. May be removed at any time."""
        _splj = {}
        _splj["type"] = self._type
        _splj["value"] = self._value
        return _splj

    def __str__(self):
        return str(self._value)
