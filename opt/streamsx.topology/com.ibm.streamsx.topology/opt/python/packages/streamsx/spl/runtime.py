# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016,2017
#
# Wrap the operator's iterable in a function
# that when called returns each value from
# the iteration returned by iter(callable).
# It the iteration returns None then that
# value is skipped (i.e. no tuple will be
# generated). When the iteration stops
# the wrapper function returns None.
#
def _splpy_iter_source(iterable) :
  it = iter(iterable)
  def _wf():
     try:
        while True:
            tv = next(it)
            if tv is not None:
                return tv
     except StopIteration:
       return None
  return _wf


# The decorated operators only support converting
# Python tuples or a list of Python tuples to
# an SPL output tuple. To simplify the generated code
# we handle any other type by using a wrapper function
# and converting to a Python tuple or list of Python
# tuples.
#
# A Python tuple returned by the wrapped function
# may be sparse, values not set by the dictionary
# (etc.) are set to None in the Python tuple.

def _splpy_to_tuples(fn, attributes):
   attr_count = len(attributes)
   attr_map = dict()
   for idx, name in enumerate(attributes):
       attr_map[name] = idx
   def _dict_to_tuple(value):
      if isinstance(value, dict):
         to_assign = set.intersection(set(value.keys()), attributes) 
         tl = [None] * attr_count
         for name in to_assign:
             tl[attr_map[name]] = value[name]
         return tuple(tl)
      return value

   def _to_tuples(*args, **kwargs):
      value = fn(*args, **kwargs)
      if isinstance(value, tuple):
          return value
      if isinstance(value, dict):
         return _dict_to_tuple(value)
      if isinstance(value, list):
         lt = list()
         for ev in value:
             if isinstance(ev, dict):
                ev = _dict_to_tuple(ev)
             lt.append(ev)
         return lt
      return value
   if hasattr(fn, '_shutdown'):
       def _shutdown():
           fn._shutdown()
       _to_tuples._shutdown = _shutdown
   return _to_tuples

def _splpy_release_memoryviews(*args):
    for o in args:
        if isinstance(o, memoryview):
            o.release()
        elif isinstance(o, list):
            for e in o:
                _splpy_release_memoryviews(e)
        elif isinstance(o, dict):
            for e in o.values():
                _splpy_release_memoryviews(e)
