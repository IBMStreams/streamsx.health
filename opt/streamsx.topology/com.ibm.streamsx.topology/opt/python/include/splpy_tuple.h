/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2017
*/

/*
 * Internal header file supporting Python
 * for com.ibm.streamsx.topology.
 *
 * This is not part of any public api for
 * the toolkit or toolkit with decorated
 * SPL Python operators.
 */


#ifndef __SPL__SPLPY_TUPLE_H
#define __SPL__SPLPY_TUPLE_H

#include "splpy_general.h"

/**
 * Structure representing a SPL tuple containing a PyObject * pointer.
 */
#define STREAMSX_TPP_PICKLE ((unsigned char) 0x80)
// Values must be non-ASCII characters as default
// format for Python 2 is ASCII with no special header.
#define STREAMSX_TPP_PTR ((unsigned char) 0x8F)
#define STREAMSX_TPP_EMPTY ((unsigned char) 0x85)
struct __SPLTuplePyPtr {
    unsigned char fmt;
    PyObject * pyptr;
};

namespace streamsx {
  namespace topology {

  /**
   * Call a Python function passing in the SPL tuple as 
   * the single element of a Python tuple.
   * Steals the reference to value.
   */
  inline PyObject * pyCallTupleFunc(PyObject *function, PyObject *pyTuple) {

      PyObject * pyReturnVar = PyObject_CallObject(function, pyTuple);
      Py_DECREF(pyTuple);

      return pyReturnVar;
    }

  /**
   * Convert the SPL tuple that represents a Python object
   * to a Python tuple that holds as its first argument:
   * One or two arguments in a Python tuple which is passed to the function
   * (which is a wrapper around the user function).
   * 
   *  If Python object was passed by reference in the SPL tuple:
   *
   *     (object) - pickle marker defaults to None to indicate actual object being passed
   *
   *  If Python object was passed as pickled bytes in the SPL tuple
   *
   *  (pv, pv) - where pv is the pickled value as a memory view object.
   *             the second value is just the marker (not None) to indicate
   *             the value needs to be depickled.
   *
   *  This means to the functions that handle pickle style
   *  on input two arguments are passed. The first with
   *  the value and the second None or the value to indicate if it
   *  is pickled.
   */
  inline PyObject * pySplProcessTuple(PyObject * function, const SPL::blob & pyo) {
      unsigned char const *data = pyo.getData();
      unsigned char fmt = *data;

      PyObject *pyTuple;
      if (fmt == STREAMSX_TPP_PTR) {

          // The fact it was passed to us must mean there is a
          // reference count we can steal which is then stolen
          // by the insertion into the Python tuple.
          __SPLTuplePyPtr *stp = (__SPLTuplePyPtr *)(data);
          PyObject * value = stp->pyptr;

          pyTuple = PyTuple_New(1);
          PyTuple_SET_ITEM(pyTuple, 0, value);
      }
      // Anything ASCII is also Pickle (Python 2 default format)
      else if (fmt <= STREAMSX_TPP_PICKLE) {
          PyObject * value = pySplValueToPyObject(pyo);

          pyTuple = PyTuple_New(2);
          PyTuple_SET_ITEM(pyTuple, 0, value);

          // Pass a non-None value as the "pickle marker (pm)"
          // simply use the same value bumping its ref.
          // 'pm' is only checked for not being None
          // in the wrapper Python function.
          Py_INCREF(value);
          PyTuple_SET_ITEM(pyTuple, 1, value);
      }
      else {
          throw SPL::SPLRuntimeDeserializationException("pySplProcessTuple", "Invalid blob");
      }

      return pyCallTupleFunc(function, pyTuple);
  }

  inline PyObject * pySplProcessTuple(PyObject * function, const SPL::rstring & pys) {
      PyObject *stringValue = pySplValueToPyObject(pys);

      PyObject * pyTuple = PyTuple_New(1);
      PyTuple_SET_ITEM(pyTuple, 0, stringValue);

      return pyCallTupleFunc(function, pyTuple);
  }

  inline PyObject * pySplProcessTuple(PyObject * function, PyObject * pyv) {

      PyObject * pyTuple = PyTuple_New(1);
      PyTuple_SET_ITEM(pyTuple, 0, pyv);

      return pyCallTupleFunc(function, pyTuple);
  }
    /**
     * Call a Python function passing in the SPL tuple as 
     * the single element of a Python tuple.
     * Steals the reference to value.
    */
    inline PyObject * pyTupleFunc(PyObject * function, PyObject * value) {
      PyObject * pyTuple = PyTuple_New(1);
      PyTuple_SET_ITEM(pyTuple, 0, value);

      return pyCallTupleFunc(function, pyTuple);
    }

    /**
     * Set a blob in an output tuple to be a pass-by-reference
     * of the PyObject pointer.
     */
    inline void pyTupleByRef(SPL::blob & retSplVal, PyObject *value, int32_t occ) {
       __SPLTuplePyPtr stpp;
       stpp.fmt = STREAMSX_TPP_PTR;
       stpp.pyptr = value;

       if (occ > 1) {
           // We already hold one reference count to
           // actually have the object, so we never decrement
           // that and instead bump by (occ-1)
           for (int i = 1; i < occ; i++)
               Py_INCREF(value);
       }

       retSplVal.setData((unsigned char const *) &stpp, sizeof(__SPLTuplePyPtr));
    }
}
}
#endif
