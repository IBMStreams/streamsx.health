# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2017
"""
Overview
========
SPL primitive operators that call a Python function or
callable class are created by decorators provided by this module.

The name of the function or callable class becomes the name of the
operator.

.. warning::
    Operator names must be valid SPL identifers,
    SPL identifiers start with an ASCII letter or underscore,
    followed by ASCII letters, digits, or underscores.
    The name also must not be an SPL keyword.

Once created the operators become part of a toolkit and may be used
like any other SPL operator.

Decorating a Python class creates a stateful SPL operator. The instance fields of the class are the state for the operator. Any parameters to the
`__init__` method (exluding the first `self` parameter) are mapped to
operator parameters.

Python classes as SPL operators
===============================

Overview
--------

Decorating a Python class creates a stateful SPL operator
where the instance fields of the class are the operator's state. An instance
of the class is created when the SPL operator invocation is initialized
at SPL runtime. The instance of the Python class is private to the SPL
operator and is maintained for the lifetime of the operator.

If the class has instance fields then they are the state of the
operator and are private to each invocation of the operator.

If the `__init__` method has parameters beyond the first
`self` parameter then they are mapped to operator parameters.
Any parameter that has a default value becomes an optional parameter
to the SPL operator. Parameters of the form `\*args` and `\*\*kwargs`
are not supported.

.. warning::
    Parameter names must be valid SPL identifers,
    SPL identifiers start with an ASCII letter or underscore,
    followed by ASCII letters, digits, or underscores.
    The name also must not be an SPL keyword.

    Parameter names ``suppress`` and ``include`` are reserved.

The value of the operator parameters at SPL operator invocation are passed
to the `__init__` method. This is equivalent to creating an instance
of the class passing the operator parameters into the constructor.

For example, with this decorated class producing an SPL source
operator::

    @spl.source()
    class Range:
      def __init__(self, stop, start=0):
        self.start = start
        self.stop = stop

      def __iter__(self):
          return zip(range(self.start, self.stop))

The SPL operator `Range` has two parameters, `stop` is mandatory and `start` is optional, defaulting to zero. Thus the SPL operator may be invoked as::

    // Produces the sequence of values from 0 to 99
    //
    // Creates an instance of the Python class
    // Range using Range(100)
    //
    stream<int32 seq> R = Range() {
      param
        stop: 100;
    }

or both operator parameters can be set::

    // Produces the sequence of values from 50 to 74
    //
    // Creates an instance of the Python class
    // Range using Range(75, 50)
    //
    stream<int32 seq> R = Range() {
      param
        start: 50;
        stop: 75;
    }

Operator state
--------------

Use of a class allows the operator to be stateful by maintaining state in instance
attributes across invocations (tuple processing).

.. note::
    For future compatibility instances of a class should ensure that the object's
    state can be pickled. See https://docs.python.org/3.5/library/pickle.html#handling-stateful-objects

Operator initialization & shutdown
----------------------------------

Execution of an instance for an operator effectively run in a context manager so that an instance's ``__enter__``
method is called when the processing element containing the operator is initialized
and its ``__exit__`` method called when the processing element is stopped. To take advantage of this
the class must define both ``__enter__`` and ``__exit__`` methods.

.. note::
    For future compatibility operator initialization such as opening files should be in ``__enter__``
    in order to support stateful operator restart & checkpointing in the future.

Example of using ``__enter__`` and ``__exit__`` to open and close a file::

    import streamsx.ec as ec

    @spp.map()
    class Sentiment(object):
        def __init__(self, name):
            self.name = name
            self.file = None

        def __enter__(self):
            self.file = open(self.name, 'r')

        def __exit__(self, exc_type, exc_value, traceback):
            if self.file is not None:
                self.file.close()

Application log and trace
-------------------------

IBM Streams provides application trace and log services which are
accesible through standard Python loggers from the `logging` module.

See :ref:`streams_app_log_trc`.

Python functions as SPL operators
=================================
Decorating a Python function creates a stateless SPL operator.
In SPL terms this is similar to an SPL Custom operator, where
the code in the Python function is the custom code. For
operators with input ports the function is called for each
input tuple, passing a Python representation of the SPL input tuple.
For an SPL source operator the function is called to obtain an iterable
whose contents will be submitted to the output stream as SPL tuples.

Operator parameters are not supported.

An example SPL sink operator that prints each input SPL tuple after
its conversion to a Python tuple::

    @spl.for_each()
    def PrintTuple(*tuple):
        "Print each tuple to standard out."
         print(tuple, flush=True)

.. _spl-tuple-to-python:

Processing SPL tuples in Python
===============================

SPL tuples are converted to Python objects and passed to a decorated callable.

Overview
--------

For each SPL tuple arriving at an input port a Python function is called with
the SPL tuple converted to Python values suitable for the function call.
How the tuple is passed is defined by the tuple passing style.

Tuple Passing Styles
--------------------

An input tuple can be passed to Python function using a number of different styles:
 * *dictionary*
 * *tuple*
 * *attributes by name* **not yet implemented**
 * *attributes by position*

Dictionary
++++++++++

Passing the SPL tuple as a Python dictionary is flexible
and makes the operator independent of any schema.
A disadvantage is the reduction in code readability
for Python function by not having formal parameters,
though getters such as ``tuple['id']`` mitigate that to some extent.
If the function is general purpose and can derive meaning
from the keys that are the attribute names then ``**kwargs`` can be useful.

When the only function parameter is ``**kwargs``
(e.g. ``def myfunc(**tuple):``) then the passing style is *dictionary*.

All of the attributes are passed in the dictionary
using the SPL schema attribute name as the key.

Tuple
+++++

Passing the SPL tuple as a Python tuple is flexible
and makes the operator independent of any schema
but is brittle to changes in the SPL schema.
Another disadvantage is the reduction in code readability
for Python function by not having formal parameters.
However if the function is general purpose and independent
of the tuple contents ``*args`` can be useful.

When the only function parameter is ``*args``
(e.g. ``def myfunc(*tuple):``) then the passing style is *tuple*.

All of the attributes are passed as a Python tuple
with the order of values matching the order of the SPL schema.

Attributes by name
++++++++++++++++++
(**not yet implemented**)

Passing attributes by name can be robust against changes
in the SPL scheme, e.g. additional attributes being added in
the middle of the schema, but does require that the SPL schema
has matching attribute names.

When *attributes by name* is used then SPL tuple attributes
are passed to the function by name for formal parameters.
Order of the attributes and parameters need not match.
This is supported for function parameters of
kind ``POSITIONAL_OR_KEYWORD`` and ``KEYWORD_ONLY``.

If the function signature also contains a parameter of the form
``**kwargs`` (``VAR_KEYWORD``) then any attributes not bound to
formal parameters are passed in its dictionary using the
SPL schema attribute name as the key.

If the function signature also contains an arbitrary argument
list ``*args`` then any attributes not bound to formal parameters
or to ``**kwargs`` are passed in order of the SPL schema.

If there are only formal parameters any non-bound attributes
are not passed into the function.

Attributes by position
++++++++++++++++++++++

Passing attributes by position allows the SPL operator to
be independent of the SPL schema but is brittle to
changes in the SPL schema. For example a function expecting
an identifier and a sensor reading as the first two attributes
would break if an attribute representing region was added as
the first SPL attribute.

When *attributes by position* is used then SPL tuple attributes are
passed to the function by position for formal parameters.
The first SPL attribute in the tuple is passed as the first parameter.
This is supported for function parameters of kind `POSITIONAL_OR_KEYWORD`.

If the function signature also contains an arbitrary argument
list `\*args` (`VAR_POSITIONAL`) then any attributes not bound
to formal parameters are passed in order of the SPL schema.

The function signature must not contain a parameter of the form
``**kwargs`` (`VAR_KEYWORD`).

If there are only formal parameters any non-bound attributes
are not passed into the function.

The SPL schema must have at least the number of positional arguments
the function requires.

Selecting the style
+++++++++++++++++++

For signatures only containing a parameter of the form 
``*args`` or ``**kwargs`` the style is implicitly defined:

 * ``def f(**tuple)`` - *dictionary* - ``tuple`` will contain a dictionary of all of the SPL tuple attribute's values with the keys being the attribute names.
 * ``def f(*tuple)`` - *tuple* - ``tuple`` will contain all of the SPL tuple attribute's values in order of the SPL schema definition.

Otherwise the style is set by the ``style`` parameter to the decorator,
defaulting to *attributes by name*. The style value can be set to:
  * ``'name'`` - *attributes by name* (the default)
  * ``'position'`` - *attributes by position*

**Note**: For backwards compatibility ``@spl.pipe`` and ``@spl.sink``
**always** use *attributes by position* and do not support ``**kwargs``.
They do not support the ``style`` parameter.

Examples
++++++++

These examples show how an SPL tuple with the schema and value::

    tuple<rstring id, float64 temp, boolean increase>
    {id='battery', temp=23.7, increase=true}

is passed into a variety of functions by showing the effective Python
call and the resulting values of the function's parameters.

*Dictionary* consuming all attributes by ``**kwargs``::

    @spl.map()
    def f(**tuple)
        pass
    # f({'id':'battery', 'temp':23.7, 'increase': True})
    #     tuple={'id':'battery', 'temp':23.7, 'increase':True}

*Tuple* consuming all attributes by ``*args``::

    @spl.map()
    def f(*tuple)
        pass
    # f('battery', 23.7, True)
    #     tuple=('battery',23.7, True)

*Attributes by name* consuming all attributes::

    @spl.map()
    def f(id, temp, increase)
        pass
    # f(id='battery', temp=23.7, increase=True)
    #     id='battery'
    #     temp=23.7
    #     increase=True

*Attributes by name* consuming a subset of attributes::

    @spl.map()
    def f(id, temp)
        pass
    # f(id='battery', temp=23.7)
    #    id='battery'
    #    temp=23.7

*Attributes by name* consuming a subset of attributes in a different order::

    @spl.map()
    def f(increase, temp)
        pass
    # f(temp=23.7, increase=True)
    #    increase=True
    #    temp=23.7

*Attributes by name* consuming `id` by name and remaining attributes by ``**kwargs``::

    @spl.map()
    def f(id, **tuple)
        pass
    # f(id='battery', {'temp':23.7, 'increase':True})
    #    id='battery'
    #    tuple={'temp':23.7, 'increase':True}

*Attributes by name* consuming `id` by name and remaining attributes by ``*args``::

    @spl.map()
    def f(id, *tuple)
        pass
    # f(id='battery', 23.7, True)
    #    id='battery'
    #    tuple=(23.7, True)

*Attributes by position* consuming all attributes::

    @spl.map(style='position')
    def f(key, value, up)
         pass
    # f('battery', 23.7, True)
    #    key='battery'
    #    value=23.7
    #    up=True

*Attributes by position* consuming a subset of attributes::

    @spl.map(style='position')
    def f(a, b)
       pass
    # f('battery', 23.7)
    #    a='battery'
    #    b=23.7

*Attributes by position* consuming `id` by position and remaining attributes by ``*args``::

    @spl.map(style='position')
    def f(key, *tuple)
        pass
    # f('battery', 23.7, True)
    #    key='battery'
    #    tuple=(23.7, True)

In all cases the SPL tuple must be able to provide all parameters
required by the function. If the SPL schema is insufficient then
an error will result, typically an SPL compile time error.

The SPL schema can provide a subset of the formal parameters if the
remaining attributes are optional (having a default).

*Attributes by name* consuming a subset of attributes with an optional parameter not matched by the schema::

    @spl.map()
    def f(id, temp, pressure=None)
       pass
    # f(id='battery', temp=23.7)
    #     id='battery'
    #     temp=23.7
    #     pressure=None

.. _submit-from-python:

Submission of SPL tuples from Python
------------------------------------

The return from a decorated callable results in submission of SPL tuples
on the associated outut port.

A Python function must return:
 * ``None``
 * a Python tuple
 * a Python dictionary
 * a list containing any of the above.

None
++++

When ``None`` is return then no tuple will be submitted to
the operator output port.

Python tuple
++++++++++++

When a Python tuple is returned it is converted to an SPL tuple and
submitted to the output port.

The values of a Python tuple are assigned to an output SPL tuple by position,
so the first value in the Python tuple is assigned to the first attribute
in the SPL tuple::

    # SPL input schema: tuple<int32 x, float64 y>
    # SPL output schema: tuple<int32 x, float64 y, float32 z>
    @spl.pipe
    def myfunc(a,b):
       return (a,b,a+b)

    # The SPL output will be:
    # All values explictly set by returned Python tuple
    # based on the x,y values from the input tuple
    # x is set to: x 
    # y is set to: y
    # z is set to: x+y

The returned tuple may be *sparse*, any attribute value in the tuple
that is ``None`` will be set to their SPL default or copied from the
input tuple, depending on the operator kind::
    
    # SPL input schema: tuple<int32 x, float64 x>
    # SPL output schema: tuple<int32 x, float64 y, float32 z>
    @spl.pipe
    def myfunc(a,b):
       return (a,None,a+b)

    # The SPL output will be:
    # x is set to: x (explictly set by returned Python tuple)
    # y is set to: y (set by matching input SPL attribute)
    # z is set to: x+y

When a returned tuple has less values than attributes in the SPL output
schema the attributes not set by the Python function will be set
to their SPL default or copied from the input tuple, depending on
the operator kind::
    
    # SPL input schema: tuple<int32 x, float64 x>
    # SPL output schema: tuple<int32 x, float64 y, float32 z>
    @spl.pipe
    def myfunc(a,b):
       return a,

    # The SPL output will be:
    # x is set to: x (explictly set by returned Python tuple)
    # y is set to: y (set by matching input SPL attribute)
    # z is set to: 0 (default int32 value)

When a returned tuple has more values than attributes in the SPL output schema then the additional values are ignored::

    # SPL input schema: tuple<int32 x, float64 x>
    # SPL output schema: tuple<int32 x, float64 y, float32 z>
    @spl.pipe
    def myfunc(a,b):
       return (a,b,a+b,a/b)

    # The SPL output will be:
    # All values explictly set by returned Python tuple
    # based on the x,y values from the input tuple
    # x is set to: x
    # y is set to: y
    # z is set to: x+y
    #
    # The fourth value in the tuple a/b = x/y is ignored.

Python dictionary
+++++++++++++++++
A Python dictionary is converted to an SPL tuple for submission to
the associated output port. An SPL attribute is set from the
dictionary if the dictionary contains a key equal to the attribute
name. The value is used to set the attribute, unless the attribute is
``None``.

If the value in the dictionary is ``None`` or no matching key exists
then the attribute value is set fom the input tuple or to its
default value depending on the operator kind.

Any keys in the dictionary that do not map to SPL attribute names are ignored.
    
Python list
+++++++++++
When a list is returned, each value is converted to an SPL tuple and
submitted to the output port, in order of the list starting with the
first element (position 0). If the list contains `None` at an index
then no SPL tuple is submitted for that index.

The list must only contain Python tuples, dictionaries or `None`. The list
can contain a mix of valid values.

The list may be empty resulting in no tuples being submitted.
"""

from enum import Enum
import functools
import inspect
import re
import sys
import streamsx.ec as ec

############################################
# setup for function inspection
if sys.version_info.major == 3:
  _inspect = inspect
#elif sys.version_info.major == 2:
#  import funcsigs
#  _inspect = funcsigs
else:
  raise ValueError("Python version not supported.")
############################################

_OperatorType = Enum('_OperatorType', 'Ignore Source Sink Pipe Filter')
_OperatorType.Source.spl_template = 'PythonFunctionSource'
_OperatorType.Pipe.spl_template = 'PythonFunctionPipe'
_OperatorType.Sink.spl_template = 'PythonFunctionSink'
_OperatorType.Filter.spl_template = 'PythonFunctionFilter'

_SPL_KEYWORDS = {'graph', 'stream', 'public', 'composite', 'input', 'output', 'type', 'config', 'logic',
                 'window', 'param', 'onTuple', 'onPunct', 'onProcess', 'state', 'stateful', 'mutable',
                 'if', 'for', 'while', 'break', 'continue', 'return', 'attribute', 'function', 'operator'}

def _valid_identifier(id):
    if re.fullmatch('[a-zA-Z_][a-zA-Z_0-9]*', id) is None or id in _SPL_KEYWORDS:
        raise ValueError("{0} is not a valid SPL identifier".format(id))

def _valid_op_parameter(name):
    _valid_identifier(name)
    if name in ['suppress', 'include']:
        raise ValueError("Parameter name {0} is reserved".format(name))

def pipe(wrapped):
    """
    Decorator to create an SPL operator from a function.
    
    A pipe SPL operator with a single input port and a single
    output port. For each tuple on the input port the
    function is called passing the contents of the tuple.

    SPL attributes from the tuple are passed by position.
    
    The value returned from the function results in
    zero or more tuples being submitted to the operator output
    port, see :ref:`submit-from-python`.
    """
    if not inspect.isfunction(wrapped):
        raise TypeError('A function is required')

    return _wrapforsplop(_OperatorType.Pipe, wrapped, 'position', False)

#
# Wrap object for an SPL operator, either
# a callable class or function.
#
def _wrapforsplop(optype, wrapped, style, docpy):

    if inspect.isclass(wrapped):
        if not callable(wrapped):
            raise TypeError('Class must be callable')

        _valid_identifier(wrapped.__name__)

        class _op_class(object):

            __doc__ = wrapped.__doc__
            @functools.wraps(wrapped.__init__)
            def __init__(self,*args,**kwargs):
                self.__splpy_instance = wrapped(*args,**kwargs)
                if ec._is_supported():
                    ec._save_opc(self.__splpy_instance)
                ec._callable_enter(self.__splpy_instance)

            if hasattr(wrapped, "__call__"):
                @functools.wraps(wrapped.__call__)
                def __call__(self, *args,**kwargs):
                    return self.__splpy_instance.__call__(*args, **kwargs)

            if hasattr(wrapped, "__iter__"):
                @functools.wraps(wrapped.__iter__)
                def __iter__(self):
                    return self.__splpy_instance.__iter__()

            def _shutdown(self):
                ec._callable_exit_clean(self.__splpy_instance)

        _op_class.__wrapped__ = wrapped
        # _op_class.__doc__ = wrapped.__doc__
        _op_class.__splpy_optype = optype
        _op_class.__splpy_callable = 'class'
        _op_class.__splpy_style = _define_style(wrapped, wrapped.__call__, style)
        _op_class.__splpy_file = inspect.getsourcefile(wrapped)
        _op_class.__splpy_docpy = docpy
        return _op_class
    if not inspect.isfunction(wrapped):
        raise TypeError('A function or callable class is required')

    _valid_identifier(wrapped.__name__)

    @functools.wraps(wrapped)
    def _op_fn(*args, **kwargs):
        return wrapped(*args, **kwargs)
    _op_fn.__splpy_optype = optype
    _op_fn.__splpy_callable = 'function'
    _op_fn.__splpy_style = _define_style(wrapped, wrapped, style)
    _op_fn.__splpy_file = inspect.getsourcefile(wrapped)
    _op_fn.__splpy_docpy = docpy
    return _op_fn

# define the SPL tuple passing style based
# upon the function signature and the decorator
# style parameter
def _define_style(wrapped, fn, style):
    has_args = False
    has_kwargs = False
    has_positional = False
    req_named = False
     
    pmds = _inspect.signature(fn).parameters
    itpmds = iter(pmds)
    # Skip self
    if inspect.isclass(wrapped):
        next(itpmds)

    pc = 0
    for pn in itpmds:
        pmd = pmds[pn]
        if pmd.kind == _inspect.Parameter.POSITIONAL_ONLY:
            raise TypeError('Positional only parameters are not supported:' + pn)
        elif pmd.kind == _inspect.Parameter.VAR_POSITIONAL:
            has_args = True
        elif pmd.kind == _inspect.Parameter.VAR_KEYWORD:
            has_kwargs = True
        elif pmd.kind == _inspect.Parameter.POSITIONAL_OR_KEYWORD:
            has_positional = True
        elif pmd.kind == _inspect.Parameter.KEYWORD_ONLY:
            if pmd.default is _inspect.Parameter.empty:
                req_named = True
        pc +=1
               
    # See if the requested style matches the signature.
    if style == 'position':
        if req_named:
             raise TypeError("style='position' not supported with a required named parameter.")
        elif pc == 1 and has_kwargs:
            raise TypeError("style='position' not supported with single **kwargs parameter.")
        elif pc == 1 and has_args:
            pass
        elif not has_positional:
            raise TypeError("style='position' not supported as no positional parameters exist.")
        # From an implementation point of view the values
        # are passed as a tuple and Python does the correct mapping
        style = 'tuple'

    elif style == 'name':
        if pc == 1 and has_args:
            raise TypeError("style='name' not supported with single *args parameter.")
        elif pc == 1 and has_kwargs:
            raise TypeError("style='name' not supported with single **kwargs parameter.")

    elif style is not None:
        raise TypeError("style=" + style + " unknown.")

    if style is None:
        if pc == 1 and has_kwargs:
            style = 'dictionary'
        elif pc == 1 and has_args:
            style = 'tuple'
        elif pc == 0:
            style = 'tuple'
        else:
            # Default to by name
            style = 'name'

    if style == 'tuple' and has_kwargs:
         raise TypeError("style='position' not implemented with **kwargs parameter.")

    if style == 'name':
         raise TypeError("Not yet implemented!")
    return style

class source:
    """
    Create a source SPL operator from an iterable.
    The resulting SPL operator has a single output port.

    When decorating a class the class must be iterable
    having an ``__iter__`` function. When the SPL operator
    is invoked an instance of the class is created
    and an iteration is created using ``iter(instance)``. 

    When decoratiing a function the function must have no
    parameters and must return an iterable or iteration.
    When the SPL operator is invoked the function is called
    and an iteration is created using ``iter(value)``
    where ``value`` is the return of the function.

    For each value in the iteration SPL zero or more tuples
    are submitted to the output port, derived from the value,
    see :ref:`submit-from-python`.
    
    If the iteration completes then no more tuples
    are submitted and a final punctuation mark
    is submitted to the output port.

    Args:
       docpy: Copy Python docstrings into SPL operator model for SPLDOC.
    """
    def __init__(self, docpy=True):
        self.style = None
        self.docpy = docpy
    
    def __call__(self, wrapped):
        return _wrapforsplop(_OperatorType.Source, wrapped, self.style, self.docpy)

class map:
    """
    Decorator to create a map SPL operator from a callable class or function.

    Creates an SPL operator with a single input port and a single
    output port. For each tuple on the input port the
    callable is called passing the contents of the tuple.

    The value returned from the callable results in
    zero or more tuples being submitted to the operator output
    port, see :ref:`submit-from-python`.

    Args:
       style: How the SPL tuple is passed into Python callable or function, see  :ref:`spl-tuple-to-python`.
       docpy: Copy Python docstrings into SPL operator model for SPLDOC.
    """
    def __init__(self, style=None, docpy=True):
        self.style = style
        self.docpy = docpy
    
    def __call__(self, wrapped):
        return _wrapforsplop(_OperatorType.Pipe, wrapped, self.style, self.docpy)

class filter(object):
    """
    Decorator that creates a filter SPL operator from a callable class or function.

    A filter SPL operator has a single input port and one mandatory
    and one optional output port. The schema of each output port
    must match the input port. For each tuple on the input port the
    callable is called passing the contents of the tuple. if the
    function returns a value that evaluates to True then it is
    submitted to mandatory output port 0. Otherwise it it submitted to
    the second optional output port (1) or discarded if the port is
    not specified in the SPL invocation.

    Args:
       style: How the SPL tuple is passed into Python callable or function, see  :ref:`spl-tuple-to-python`.
       docpy: Copy Python docstrings into SPL operator model for SPLDOC.

    Example definition::

        @spl.filter()
        class AttribThreshold(object):
            \"\"\"
            Filter based upon a single attribute being
            above a threshold.
            \"\"\"
            def __init__(self, attr, threshold):
                self.attr = threshold
                self.threshold = threshold
                
            def __call__(self, **tuple):
                return tuple[self.attr] > self.threshold:

    Example SPL invocation::

        stream<rstring id, float64 voltage> Sensors = ...
        stream<Sensors> InterestingSensors = AttribThreshold(Sensors) {
            param 
              attr: "voltage";
              threshold: 225.0;
        }
    """
    def __init__(self, style=None, docpy=True):
        self.style = style
        self.docpy = docpy
    
    def __call__(self, wrapped):
        return _wrapforsplop(_OperatorType.Filter, wrapped, self.style, self.docpy)

def ignore(wrapped):
    """
     Decorator to ignore a Python function.

     If a Python callable is decorated with ``@spl.ignore``
     then function is ignored by ``spl-python-extract.py``.

     Args:
         wrapped: Function that will be ignored.
    """
    @functools.wraps(wrapped)
    def _ignore(*args, **kwargs):
        return wrapped(*args, **kwargs)
    _ignore.__splpy_optype = _OperatorType.Ignore
    _ignore.__splpy_file = inspect.getsourcefile(wrapped)
    return _ignore

# Defines a function as a sink operator
def sink(wrapped):
    if not inspect.isfunction(wrapped):
        raise TypeError('A function is required')

    return _wrapforsplop(_OperatorType.Sink, wrapped, 'position', False)

# Defines a function as a sink operator
class for_each:
    """
    Decorator that creates an SPL operator from a callable class or function.

    A SPL operator with a single input port and no output ports.
    For each tuple on the input port the decorated callable
    is called passing the contents of the tuple.

    Args:
       style: How the SPL tuple is passed into Python callable or function, see  :ref:`spl-tuple-to-python`.
       docpy: Copy Python docstrings into SPL operator model for SPLDOC.
    """
    def __init__(self, style=None, docpy=True):
        self.style = style
        self.docpy = docpy

    def __call__(self, wrapped):
        return _wrapforsplop(_OperatorType.Sink, wrapped, self.style, self.docpy)
