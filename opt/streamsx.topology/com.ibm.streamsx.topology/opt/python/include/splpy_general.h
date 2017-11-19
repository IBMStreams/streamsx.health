/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016
*/

/*
 * Internal header file supporting Python
 * for com.ibm.streamsx.topology.
 *
 * This is not part of any public api for
 * the toolkit or toolkit with decorated
 * SPL Python operators.
 *
 * Functionality related to general
 * non-operator, non-data processing functions.
 */

#ifndef __SPL__SPLPY_GENERAL_H
#define __SPL__SPLPY_GENERAL_H

#include "Python.h"
#include <sstream>

#undef PyMemoryView_Check
#define PyMemoryView_Check(o) SplpyGeneral::checkMemoryView(o)

#include <TopologySplpyResource.h>
#include <SPL/Runtime/Common/RuntimeException.h>
#include <SPL/Runtime/Type/Meta/BaseType.h>
#include <SPL/Runtime/Function/SPLFunctions.h>
#include <SPL/Runtime/ProcessingElement/PE.h>
#include <SPL/Runtime/Operator/Port/OperatorPort.h>
#include <SPL/Runtime/Operator/Port/OperatorInputPort.h>
#include <SPL/Runtime/Operator/Port/OperatorOutputPort.h>
#include <SPL/Runtime/Operator/OperatorContext.h>
#include <SPL/Runtime/Operator/Operator.h>

namespace streamsx {
  namespace topology {

    inline PyObject * _pyUnicode_FromUTF8(const char * str, int len) {
        PyObject * val = PyUnicode_DecodeUTF8(str, len, NULL);
        return val;
    }

    inline PyObject * pyUnicode_FromUTF8(const char * str) {
        return _pyUnicode_FromUTF8(str, strlen(str));
    }
    inline PyObject * pyUnicode_FromUTF8(std::string const & str) {
        return _pyUnicode_FromUTF8(str.c_str(), str.size());
    }


    /*
    ** Convert to a SPL rstring from a Python string object.
    ** Returns 0 if successful, non-zero if error.
    */
    inline int pyRStringFromPyObject(SPL::rstring & attr, PyObject * value) {
      Py_ssize_t size = 0;
      PyObject * converted = NULL;
      char * bytes = NULL;

#if PY_MAJOR_VERSION == 3
      // Python 3 character strings are unicode objects
      // Caller is not responsible for deallocating buffer
      // returned by PyUnicode_AsUTF8AndSize
      //
      if (!PyUnicode_Check(value)) {
          // Create a string from the object
          value = converted = PyObject_Str(value);
      }
      bytes = PyUnicode_AsUTF8AndSize(value, &size);
#else
      // Python 2 supports Unicode and byte character strings 
      // Default is byte character strings.
      // PyString_AsStringAndSize returns a pointer to an
      // internal buffer that must not be modified or deallocated
      if (PyUnicode_Check(value)) {
          value = converted = PyUnicode_AsUTF8String(value);
      } else if (PyString_Check(value)) {
           // no coversion needed
      } else {
          // Create a string from the object
          value = converted = PyObject_Str(value);
      }
      int rc = PyString_AsStringAndSize(value, &bytes, &size);
      if (rc != 0)
          bytes = NULL;
#endif

      if (bytes == NULL) {
         if (converted != NULL)
             Py_DECREF(converted);
         return -1;
      }

      // This copies from bytes into the rstring.
      attr.assign((const char *)bytes, (size_t) size);

      // Need to decrement the reference after we
      // have copied the bytes out as bytes points
      // into the Python object.
      if (converted != NULL)
          Py_DECREF(converted);

      return 0;
    }

class SplpyGIL {
   public:
        SplpyGIL() {
          gstate_ = PyGILState_Ensure();
        }
        ~SplpyGIL() {
          PyGILState_Release(gstate_);
        }
        
      private:
        PyGILState_STATE gstate_;
    };

class SplpyGeneral {

  public:
    /*
     * We load Py_None indirectly to avoid
     * having a reference to it when the
     * operator shared library is loaded.
     */
    static bool isNone(PyObject *o) {

        static PyObject * none = o;

        return o == none;
    }
    /**
      PyMemoryView_Check macro gets reassigned to
      this function. This is because using it directly
      leads to a reference to PyMemoryView_Type field
      in the operator.so which cannot be resolved until
      after the operator.so is loaded. Since this field
      is used by its address in the _Check macro we
      cannot use the weak symbol trick.
    */
    static bool checkMemoryView(PyObject * o) {
        static PyObject * memoryViewTypeAddr = o;

        return ((PyObject *) Py_TYPE(o)) == memoryViewTypeAddr;
    }

    static PyObject * getBool(const bool & value) {
       return PyBool_FromLong(value ? 1 : 0);
     }

    /**
     * Utility method to call an object
     * passing in a tuple of arguments.
     * 
     * Steals the reference the the tuple.
     */
    static PyObject *pyCallObject(PyObject *pyclass, PyObject *args) {
      PyObject *ret  = PyObject_CallObject(pyclass, args);
      Py_DECREF(args);
      if (ret == NULL) {
         throw SplpyGeneral::pythonException("pyCallObject");
      }

      return ret;
    }

    /**
     * Class object for streamsx.spl.types.Timestamp.
     * First call is through setup to set the
     * static variable.
     * Subsequent calls pass null and receive
     * the timestamp class.
     */
    static PyObject * timestampClass(PyObject *tsc) {
        static PyObject * tsClass = tsc;
        return tsClass;
    }
    static PyObject * decimalClass(PyObject *dsc) {
        static PyObject * decClass = dsc;
        return decClass;
    }
    /**
     * streamsx.spl.types._get_timestamp_tuple
     * Used to convert Python objects to SPL timestamps.
     */
    static PyObject * timestampGetter(PyObject *tsg) {
        static PyObject * tsGetter = tsg;
        return tsGetter;
    }


    /*
     * Flush Python stderr and stdout.
    */
    static void flush_PyErrPyOut() {
        PyRun_SimpleString("sys.stdout.flush()");
        PyRun_SimpleString("sys.stderr.flush()");
    }

    /*
    * Call PyErr_Print() and then flush stderr.
    * This is because CPython buffers stderr (and stdout)
    * when it is not connected to a terminal.
    * Without the flush output is lost in distributed
    * (since stderr is conncted to a file) and
    * makes diagnosing errors impossible.
    */
    static void flush_PyErr_Print() {
        if (PyErr_Occurred() != NULL)
            PyErr_Print();
        SplpyGeneral::flush_PyErrPyOut();
    }

    /**
     * Return an SPL runtime exception that can be thrown
     * based upon the Python error. Also flushes Python stdout
     * and stderr to ensure any additional info is visible
     * in the PE console.
    */
    static SPL::SPLRuntimeException pythonException(std::string const & location) {

      PyObject *pyType, *pyValue, *pyTraceback;
      PyErr_Fetch(&pyType, &pyValue, &pyTraceback);
      PyErr_NormalizeException(&pyType, &pyValue, &pyTraceback);
      
      SPL::rstring msg("Unknown Python error");
      if (pyValue != NULL) {
          pyRStringFromPyObject(msg, pyValue);
      }

      // Restore the error to get the stack trace
      // PeErr_Restore steals the references
      PyErr_Restore(pyType, pyValue, pyTraceback);
      SplpyGeneral::flush_PyErr_Print();

      SPL::SPLRuntimeOperatorException exc(location, msg);
      
      return exc;
    }

    /**
     * Return a general exception to be thrown.
     * This is for an error situation not driven
     * by a Python error. Output is still flushed
     * to ensure any output that might be relevant
     * is available.
     */
    static SPL::SPLRuntimeException generalException(
       std::string const & location, std::string const & msg) {

      SPLAPPTRC(L_ERROR, msg, "python");
      SPL::SPLRuntimeOperatorException exc(location, msg);
      
      return exc;
    }

    /*
     * Load a function or any callable in a module,
     * returning the reference to the callable.
     *
     * Caller must hold the GILState
     */
    static PyObject * loadFunction(const std::string & mn, const std::string & fn)
     {    
       PyObject * module = importModule(mn);
       PyObject * function = PyObject_GetAttrString(module, fn.c_str());
       Py_DECREF(module);
    
       if (!PyCallable_Check(function)) {
         std::stringstream msg;
         msg << "Fatal error: function " << fn << " in module " << mn << " not callable.";
         throw SplpyGeneral::generalException("setup", msg.str());
        }
        SPLAPPTRC(L_INFO, "Callable function: " << fn, "python");
        return function;
      }
    static PyObject * loadFunctionGIL(const std::string & mn, const std::string & fn)
    {    
        SplpyGIL lock;
        return loadFunction(mn, fn);
    }

    /*
     * Import a module, returning the reference to the module.
     * Caller must hold the GILState
     */
    static PyObject * importModule(const std::string & mn) 
    {
      PyObject * moduleName = pyUnicode_FromUTF8(mn.c_str());
      PyObject * module = PyImport_Import(moduleName);
      Py_DECREF(moduleName);
      if (module == NULL) {
        SPLAPPLOG(L_ERROR, TOPOLOGY_IMPORT_MODULE_ERROR(mn), "python");
        throw SplpyGeneral::pythonException(mn);
      }
      SPLAPPLOG(L_INFO, TOPOLOGY_IMPORT_MODULE(mn), "python");
      return module;
    }

    /*
    * One off generic call a function by name passing one or two arguments
    * returning its return. Used for setup calls in operator constructors
    * as the reference to the method is not kept, assuming it is not
    * called frequently.
    * References to the arguments are stolen by this function.
    */
    static PyObject * callFunction(const std::string & mn, const std::string & fn, PyObject *arg1, PyObject *arg2) {
        SPLAPPTRC(L_DEBUG, "Executing function " << mn << "." << fn , "python");
        PyObject * function = loadFunction(mn, fn);
        PyObject * funcArg = PyTuple_New(arg1 ? (arg2 ? 2 : 1) : 0);
        if (arg1)
            PyTuple_SET_ITEM(funcArg, 0, arg1);
        if (arg2)
            PyTuple_SET_ITEM(funcArg, 1, arg2);
        PyObject *ret = PyObject_CallObject(function, funcArg);
        Py_DECREF(funcArg);
        Py_DECREF(function);
        if (ret == NULL) {
            SPLAPPTRC(L_ERROR, "Failed function execution " << mn << "." << fn, "python");
            throw SplpyGeneral::pythonException(mn+"."+fn);
        }
        SPLAPPTRC(L_DEBUG, "Executed function " << mn << "." << fn , "python");
        return ret;
    }
    /**
     * Version of callFunction() that discards the returned value.
    */
    static void callVoidFunction(const std::string & mn, const std::string & fn, PyObject *arg1, PyObject * arg2) {
        PyObject * ret = callFunction(mn, fn, arg1, arg2);
        Py_DECREF(ret);
    }
};

    /*
    ** Conversion of Python objects to SPL values.
    */

    /*
    ** Convert to a SPL blob from a Python bytes object.
    */
    inline void pySplValueFromPyObject(SPL::blob & splv, PyObject * value) {
      char * bytes = NULL;
      Py_ssize_t size = -1;

      if (PyMemoryView_Check(value)) {
         Py_buffer *buf = PyMemoryView_GET_BUFFER(value);
         bytes = (char *) buf->buf;
         size = buf->len;
      }
      else
      {
          bytes = PyBytes_AsString(value);
          size = PyBytes_GET_SIZE(value);
      }

      if (size != 0 && bytes == NULL) {
         SPLAPPTRC(L_ERROR, "Python can't convert to SPL blob!", "python");
         throw SplpyGeneral::pythonException("blob");
      }

      // This takes a copy of the data.
      splv.setData((const unsigned char *)bytes, size);
    }

    /*
     * Sets the blob data to use the bytes from the PyBytes object.
     * Thus while the blob is active the reference count must
     * be held on value.
     */
    inline void pySplValueUsingPyObject(SPL::blob & splv, PyObject * value) {
      char * bytes = PyBytes_AsString(value);          
      if (bytes == NULL) {
         SPLAPPTRC(L_ERROR, "Python can't convert to SPL blob!", "python");
         throw SplpyGeneral::pythonException("blob");
      }
      long int size = PyBytes_GET_SIZE(value);
      splv.useExternalData((unsigned char *)bytes, size);
    }

    /*
    ** Convert to a SPL rstring from a Python string object.
    */
    inline void pySplValueFromPyObject(SPL::rstring & splv, PyObject * value) {
      if (pyRStringFromPyObject(splv, value) != 0) {
         SPLAPPTRC(L_ERROR, "Python can't convert to UTF-8!", "python");
         throw SplpyGeneral::pythonException("rstring");
      }
    }

    inline void pySplValueFromPyObject(SPL::ustring & splv, PyObject * value) {
         SPL::rstring rs;
         pySplValueFromPyObject(rs, value);

         splv = SPL::ustring::fromUTF8(rs);
    }

    inline SPL::rstring pyRstringFromPyObject(PyObject * value)
    {
        SPL::rstring rs;
        pySplValueFromPyObject(rs, value);
        return rs ;
    }
    inline SPL::ustring pyUstringFromPyObject(PyObject * value)
    {
        SPL::ustring us;
        pySplValueFromPyObject(us, value);
        return us ;
    }

    // signed integers
    inline void pySplValueFromPyObject(SPL::int8 & splv, PyObject * value) {
       splv = (SPL::int8) PyLong_AsLong(value);
    }
    inline void pySplValueFromPyObject(SPL::int16 & splv, PyObject * value) {
       splv = (SPL::int16) PyLong_AsLong(value);
    }
    inline void pySplValueFromPyObject(SPL::int32 & splv, PyObject * value) {
       splv = (SPL::int32) PyLong_AsLong(value);
    }
    inline void pySplValueFromPyObject(SPL::int64 & splv, PyObject * value) {
       splv = (SPL::int64) PyLong_AsLong(value);
    }

    // unsigned integers
    inline void pySplValueFromPyObject(SPL::uint8 & splv, PyObject * value) {
       splv = (SPL::uint8) PyLong_AsUnsignedLong(value);
    }
    inline void pySplValueFromPyObject(SPL::uint16 & splv, PyObject * value) {
       splv = (SPL::uint16) PyLong_AsUnsignedLong(value);
    }
    inline void pySplValueFromPyObject(SPL::uint32 & splv, PyObject * value) {
       splv = (SPL::uint32) PyLong_AsUnsignedLong(value);
    }
    inline void pySplValueFromPyObject(SPL::uint64 & splv, PyObject * value) {
       splv = (SPL::uint64) PyLong_AsUnsignedLong(value);
    }

    // boolean
    inline void pySplValueFromPyObject(SPL::boolean & splv, PyObject * value) {
       splv = PyObject_IsTrue(value);
    }
 
    // floats
    inline void pySplValueFromPyObject(SPL::float32 & splv, PyObject * value) {
       splv = (SPL::float32) PyFloat_AsDouble(value);
    }
    inline void pySplValueFromPyObject(SPL::float64 & splv, PyObject * value) {
       splv = PyFloat_AsDouble(value);
    }

    /**
     * Convert Python object to SPL timestamp:
     *
     * 1) Call streamsx.spl.types._get_timestamp_tuple to covert
     *    Python object to tuple containing:
     *      (seconds, nanoseconds, machine_id)
     * 2) Extract values from each tuple element.
     */
    inline void pySplValueFromPyObject(SPL::timestamp & splv, PyObject *value) {
        PyObject * args = PyTuple_New(1);
        Py_INCREF(value);
        PyTuple_SET_ITEM(args, 0, value);

        PyObject *tst = SplpyGeneral::pyCallObject(
                 SplpyGeneral::timestampGetter(NULL), args);
        Py_DECREF(args);

        splv.setSeconds(
            (int64_t) PyLong_AsLong(PyTuple_GET_ITEM(tst, 0)));
        splv.setNanoSeconds(
            (uint32_t) PyLong_AsUnsignedLong(PyTuple_GET_ITEM(tst, 1)));
        splv.setMachineId(
            (int32_t) PyLong_AsLong(PyTuple_GET_ITEM(tst, 2)));
    }

    // decimal
    inline void pySplValueFromPyObject(SPL::decimal32 & splv, PyObject *value) {
        SPL::rstring rs;
        pySplValueFromPyObject(rs, value);
        std::istringstream is(rs);
        SPL::deserializeWithNanAndInfs(is, splv);
    }
    inline void pySplValueFromPyObject(SPL::decimal64 & splv, PyObject *value) {
        SPL::rstring rs;
        pySplValueFromPyObject(rs, value);
        std::istringstream is(rs);
        SPL::deserializeWithNanAndInfs(is, splv);
    }
    inline void pySplValueFromPyObject(SPL::decimal128 & splv, PyObject *value) {
        SPL::rstring rs;
        pySplValueFromPyObject(rs, value);
        std::istringstream is(rs);
        SPL::deserializeWithNanAndInfs(is, splv);
    }

    // complex
    inline void pySplValueFromPyObject(SPL::complex32 & splv, PyObject * value) {
        splv = SPL::complex32(
          (SPL::float32) PyComplex_RealAsDouble(value),
          (SPL::float32) PyComplex_ImagAsDouble(value)
        );
    }
    inline void pySplValueFromPyObject(SPL::complex64 & splv, PyObject * value) {
        splv = SPL::complex64(
          (SPL::float64) PyComplex_RealAsDouble(value),
          (SPL::float64) PyComplex_ImagAsDouble(value)
        );
    }

    // SPL list from Python list
    template <typename T>
    inline void pySplValueFromPyObject(SPL::list<T> & l, PyObject *value) {
        const Py_ssize_t size = PyList_Size(value);

        for (Py_ssize_t i = 0; i < size; i++) {
            T se;
            l.add(se); // Add takes a copy of the value

            PyObject * e = PyList_GET_ITEM(value, i);
            pySplValueFromPyObject(l.at(i), e);
        }
    }
 
    // SPL set from Python set
    template <typename T>
    inline void pySplValueFromPyObject(SPL::set<T> & s, PyObject *value) {
        // validates that value is a Python set
        const Py_ssize_t size = PySet_Size(value);

        PyObject * iterator = PyObject_GetIter(value);
        if (iterator == 0) {
            throw SplpyGeneral::pythonException("iter(set)");
        }
        PyObject *item;
        while ((item = PyIter_Next(iterator))) {
            T se;
            pySplValueFromPyObject(se, item);
            Py_DECREF(item);
            s.add(se);
        }
        Py_DECREF(iterator);
    }

    // SPL map from Python dictionary
    template <typename K, typename V>
    inline void pySplValueFromPyObject(SPL::map<K,V> & m, PyObject *value) {
        PyObject *k,*v;
        Py_ssize_t pos = 0;
        while (PyDict_Next(value, &pos, &k, &v)) {
           K sk;

           // Set the SPL key
           pySplValueFromPyObject(sk, k);

           // map[] creates the value if it does not exist
           V & sv = m[sk];
 
           // Set the SPL value 
           pySplValueFromPyObject(sv, v);
        }
    }

    /**************************************************************/

    /*
    ** Conversion of SPL attributes or value to Python objects
    */

    /**
     * Integer conversions.
    */

    inline PyObject * pySplValueToPyObject(const SPL::int32 & value) {
      return PyLong_FromLong(value);
    }
    inline PyObject * pySplValueToPyObject(const SPL::int64 & value) {
      return PyLong_FromLong(value);
    }
    inline PyObject * pySplValueToPyObject(const SPL::uint32 & value) {
      return PyLong_FromUnsignedLong(value);
    }
    inline PyObject * pySplValueToPyObject(const SPL::uint64 & value) {
      return PyLong_FromUnsignedLong(value);
    }

    /**
     * Convert a SPL blob into a Python Memory view object.
     */
    inline PyObject * pySplValueToPyObject(const SPL::blob & value) {
      long int sizeb = value.getSize();
      const unsigned char * bytes = value.getData();

#if PY_MAJOR_VERSION == 3
      return PyMemoryView_FromMemory((char *) bytes, sizeb, PyBUF_READ);
#else
      Py_buffer buf;
      PyBuffer_FillInfo(&buf, NULL, (void*) bytes, (Py_ssize_t) sizeb , 1, PyBUF_SIMPLE);
      
      // PyMemoryView_FromBuffer makes a copy of the info from buf.
      return PyMemoryView_FromBuffer(&buf);
#endif
    }


    /**
     * Convert a SPL rstring into a Python Unicode string 
     */
    inline PyObject * pySplValueToPyObject(const SPL::rstring & value) {
      long int sizeb = value.size();
      const char * pybytes = value.data();

      return PyUnicode_DecodeUTF8(pybytes, sizeb, NULL);
    }
    /**
     * Convert a SPL ustring into a Python Unicode string 
     */
    inline PyObject * pySplValueToPyObject(const SPL::ustring & value) {
      long int sizeb = value.length() * 2; // Need number of bytes
      const char * pybytes =  (const char*) (value.getBuffer());

      return PyUnicode_DecodeUTF16(pybytes, sizeb, NULL, NULL);
    }

    inline PyObject * pySplValueToPyObject(const SPL::complex32 & value) {
       return PyComplex_FromDoubles(value.real(), value.imag());
    }
    inline PyObject * pySplValueToPyObject(const SPL::complex64 & value) {
       return PyComplex_FromDoubles(value.real(), value.imag());
    }

    /**
     *  Convert decimal values by first converting to strings
     *  and then creating Python decimal.Decimal instance.
     */
    inline PyObject * _pySplDecStringToPyDecimal(const std::stringstream & buf)
    {
        SPL::rstring decString(buf.str());

        PyObject * pyDecString = pySplValueToPyObject(decString);

        PyObject * pyTuple = PyTuple_New(1);
        PyTuple_SET_ITEM(pyTuple, 0, pyDecString);

        return SplpyGeneral::pyCallObject(
                 SplpyGeneral::decimalClass(NULL),
                 pyTuple
               );
    }
    inline PyObject * pySplValueToPyObject(const SPL::decimal32 & value) {

        // Number of decimal32 digits (7) minus 1 to account
        // for the single digit written before the decimal point
        // in scientific notation
        // www.cplusplus.com/reference/ios/scientific
        std::stringstream buf;
        buf.setf(std::ios::scientific, std::ios::floatfield);
        buf.precision(7 - 1);
        buf << value;

        return _pySplDecStringToPyDecimal(buf);
    }
    inline PyObject * pySplValueToPyObject(const SPL::decimal64 & value) {
        // Number of decimal64 digits (16) minus 1 to account
        // for the single digit written before the decimal point
        // in scientific notation
        // www.cplusplus.com/reference/ios/scientific
        std::stringstream buf;
        buf.setf(std::ios::scientific, std::ios::floatfield);
        buf.precision(16 - 1);
        buf << value;

        return _pySplDecStringToPyDecimal(buf);
    }
    inline PyObject * pySplValueToPyObject(const SPL::decimal128 & value) {
        // Number of decimal128 digits (34) minus 1 to account
        // for the single digit written before the decimal point
        // in scientific notation
        // www.cplusplus.com/reference/ios/scientific
        std::stringstream buf;
        buf.setf(std::ios::scientific, std::ios::floatfield);
        buf.precision(34 - 1);
        buf << value;

        return _pySplDecStringToPyDecimal(buf);
    }

    inline PyObject * pySplValueToPyObject(const SPL::boolean & value) {
       PyObject * pyValue = SplpyGeneral::getBool(value);
       return pyValue;
    }
    inline PyObject * pySplValueToPyObject(const SPL::float32 & value) {
       return PyFloat_FromDouble(value);
    }
    inline PyObject * pySplValueToPyObject(const SPL::float64 & value) {
       return PyFloat_FromDouble(value);
    }

    inline PyObject * pySplValueToPyObject(const SPL::timestamp & value) {
        int32_t mid = value.getMachineId();
        PyObject * pyTuple = PyTuple_New(mid == 0 ? 2 : 3);

        PyTuple_SET_ITEM(pyTuple, 0, pySplValueToPyObject(value.getSeconds()));
        PyTuple_SET_ITEM(pyTuple, 1, pySplValueToPyObject(value.getNanoseconds()));
        if (mid != 0) {
            PyTuple_SET_ITEM(pyTuple, 2, pySplValueToPyObject(mid));
        }
        return SplpyGeneral::pyCallObject(
                 SplpyGeneral::timestampClass(NULL),
                 pyTuple
               );
    }


    /**
     * Convert a PyObject to a PyObject by simply returning the value
     * nb. that if object has it ref count decremented to 0 the 
     * "copied" pointer is no longer valid
     */
    inline PyObject * pySplValueToPyObject(PyObject * object) {
      return object;
    }

    /*
    ** SPL List Conversion to Python list
    */
    template <typename T>
    inline PyObject * pySplValueToPyObject(const SPL::list<T> & l) {
        PyObject * pyList = PyList_New(l.size());
        for (int i = 0; i < l.size(); i++) {
            PyList_SET_ITEM(pyList, i, pySplValueToPyObject(l[i]));
        }
        return pyList;
    }

    /*
    ** SPL Map Conversion to Python dict.
    */
    template <typename K, typename V>
    inline PyObject * pySplValueToPyObject(const SPL::map<K,V> & m) {
        PyObject * pyDict = PyDict_New();
        for (typename std::tr1::unordered_map<K,V>::const_iterator it = m.begin();
             it != m.end(); it++) {
             PyObject *k = pySplValueToPyObject(it->first);
             PyObject *v = pySplValueToPyObject(it->second);
             PyDict_SetItem(pyDict, k, v);
             Py_DECREF(k);
             Py_DECREF(v);
        }
      
        return pyDict;
    }

    /*
    ** SPL Set Conversion to Python set
    */
    template <typename T>
    inline PyObject * pySplValueToPyObject(const SPL::set<T> & s) {
        PyObject * pySet = PySet_New(NULL);
        for (typename std::tr1::unordered_set<T>::const_iterator it = s.begin();
             it != s.end(); it++) {
             PyObject * e = pySplValueToPyObject(*it);
             PySet_Add(pySet, e);
             Py_DECREF(e);
        }
        return pySet;
    }

/*
 * A MemoryView from a blob attribute in an SPL schema
 * just points to the tuple memory. In 3 this is safe
 * as we release the memory view once process returns
 * using MemoryViewCleanup RAII.
 *
 * In Python2 there is no release to to allow blobs
 * in schemas we copy the contents.
 *
 * We do it this way
 * rather than in the conversion method as if the schema
 * is the python object we know we only have a reference
 * to the memoryview and thus never want to copy.
 *
 */
#if PY_MAJOR_VERSION == 3

#define PYSPL_MEMORY_VIEW_CLEANUP() MemoryViewCleanup pyMvs
#define PYSPL_MEMORY_VIEW(o) pyMvs.add(o)

/*
 * Maintains any object that is or contains a memory view object.
 * Since the memory being viewed is from the incoming SPL tuple
 * it becomes invalid once the operator process method returns.
 * This is an RAII object that will go put of scope at the
 * end of the process method, resulting in a call into Python
 * for any memory view object that is still being used or
 * any object that is a collection holding a memory view.
 */
class MemoryViewCleanup {
   public:
        MemoryViewCleanup() {
        }
        ~MemoryViewCleanup() {
             SplpyGIL lock;

             Py_ssize_t np = 0;

             // Determine how many items we need
             // to pass into Python.
             for (int i = 0; i < mvs_.size(); i++) {
                 PyObject *mv = mvs_[i];
                 if (PyMemoryView_Check(mv) && (Py_REFCNT(mv) == 1)) {
                     // Only we hold a reference to it and it's
                     // a memory view object, simply decrement 
                     Py_DECREF(mv);
                     mvs_[i] = NULL;
                     continue;
                 }
                 np++;
             }

             if (np != 0) {
                 Py_ssize_t npi = 0;
                 PyObject *args = PyTuple_New(np);
                 for (int i = 0; i < mvs_.size();i++) {
                     PyObject *mv = mvs_[i];
                     if (mv != NULL) {
                         // steals our reference
                         PyTuple_SET_ITEM(args, npi++, mv);
                     }
                 }
                 PyObject * ret = SplpyGeneral::pyCallObject(releaser(), args);
                 Py_DECREF(ret);
             }
        }
        void add(PyObject *mv) {
            Py_INCREF(mv);
            mvs_.push_back(mv);
        }
        
      private:
        std::vector<PyObject *> mvs_;

        static PyObject * releaser() {
           static PyObject * releaser = SplpyGeneral::loadFunctionGIL("streamsx.spl.runtime", "_splpy_release_memoryviews");
           return releaser;
        }
};

#else /* VER == 2 */

#define PYSPL_MEMORY_VIEW_CLEANUP() /* TODO */
#define PYSPL_MEMORY_VIEW(o) /* TODO */

#endif /* END VER 2/3 */

}}

#endif
