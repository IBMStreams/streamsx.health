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
 * Functionality related to setting up
 * Python symbols to point to dynamically
 * loaded symbols.
 */


#ifndef __SPL__SPLPY_SYM_H
#define __SPL__SPLPY_SYM_H

#include <stdexcept>
#include "Python.h"
#include "splpy_ec_api.h"

/**
 * For a Python C API function symbol PyXXX we create
 * - typedef matching the function
 *
 * - a symbol __spl_fp_PyXXX that is dlsym() resolved to PyXXX from the
 *   dynamically loaded shared library.
 *
 * - a function __spl_fp_PyXXX that invokes the symbol __spl_fp_PyXXX
 *   and thus invokes PyXXX
 *  
 * - A weak mapping from the symbol PyXXX to __spl_fp_PyXXX. This
 *   means when the operator (PE pre-4.2) shared library is opened
 *   by the SPL runtime with RTLD_NOW PyXXX will not be marked as
 *   an unresolved symbol, instead it will resolve to __spl_fp_PyXXX
 *   This allows the code to be written using the standard Python
 *   API calls, but code in this header maps them to the dynamically
 *   loaded library.
 *
 *   This is all to allow the location of the Python dynamic shared
 *   library to be set by PYTHONHOME and loaded at runtime.
 */

/**
 * Generic typedefs potentially shared by more than one function.
 */
typedef void (*__splpy_v_v_fp)(void);
typedef PyObject * (*__splpy_p_p_fp)(PyObject *);
typedef PyObject * (*__splpy_p_pp_fp)(PyObject *, PyObject *);
typedef PyObject * (*__splpy_p_ppp_fp)(PyObject *, PyObject *, PyObject *);
typedef int (*__splpy_i_pp_fp)(PyObject *, PyObject *);
typedef int (*__splpy_i_ppp_fp)(PyObject *, PyObject *, PyObject *);
typedef PyObject * (*__splpy_p_s_fp)(Py_ssize_t);
typedef PyObject * (*__splpy_v_p_fp)(void);
typedef char * (*__splpy_c_p_fp)(PyObject *);
typedef int (*__splpy_i_p_fp)(PyObject *);
typedef Py_ssize_t (*__splpy_s_p_fp)(PyObject *);
typedef long (*__splpy_l_p_fp)(PyObject *);
typedef PyObject * (*__splpy_p_l_fp)(long);
typedef PyObject * (*__splpy_p_d_fp)(double);
typedef double (*__splpy_d_p_fp)(PyObject *);

/*
 * GIL State locks
 */

typedef PyGILState_STATE (*__splpy_gil_v_fp)(void);
typedef void (*__splpy_v_gil_fp)(PyGILState_STATE);

extern "C" {
  static __splpy_gil_v_fp __spl_fp_PyGILState_Ensure;
  static __splpy_v_gil_fp __spl_fp_PyGILState_Release;

  static PyGILState_STATE __spl_fi_PyGILState_Ensure() {
     return __spl_fp_PyGILState_Ensure();
  }
  static void __spl_fi_PyGILState_Release(PyGILState_STATE state) {
     __spl_fp_PyGILState_Release(state);
  }
};
#pragma weak PyGILState_Ensure = __spl_fi_PyGILState_Ensure
#pragma weak PyGILState_Release = __spl_fi_PyGILState_Release

/*
 * String handling
 */
typedef PyObject* (*__splpy_udu_fp)(const char *, Py_ssize_t, const char *);
typedef PyObject* (*__splpy_uaus_fp)(PyObject *);

extern "C" {
  static __splpy_p_p_fp __spl_fp_PyObject_Str;
  static __splpy_udu_fp __spl_fp_PyUnicode_DecodeUTF8;
  static __splpy_uaus_fp __spl_fp_PyUnicode_AsUTF8String;
  static __splpy_c_p_fp __spl_fp_PyBytes_AsString;

  static PyObject * __spl_fi_PyObject_Str(PyObject *v) {
     return __spl_fp_PyObject_Str(v);
  }
  static PyObject * __spl_fi_PyUnicode_DecodeUTF8(const char *s, Py_ssize_t size, const char * errors) {
     return __spl_fp_PyUnicode_DecodeUTF8(s, size, errors);
  }
  static PyObject * __spl_fi_PyUnicode_AsUTF8String(PyObject *s) {
     return __spl_fp_PyUnicode_AsUTF8String(s);
  }
  static char * __spl_fi_PyBytes_AsString(PyObject * o) {
     return __spl_fp_PyBytes_AsString(o);
  }
}
#pragma weak PyObject_Str = __spl_fi_PyObject_Str

#if PY_MAJOR_VERSION == 3
#pragma weak PyUnicode_DecodeUTF8 = __spl_fi_PyUnicode_DecodeUTF8
#pragma weak PyUnicode_AsUTF8String = __spl_fi_PyUnicode_AsUTF8String
#pragma weak PyBytes_AsString = __spl_fi_PyBytes_AsString
#else
// In Python2 the original functions (e.g. PyUnicode_DecodeUTF8)
// are #defined to different functions and hence symbols.
#pragma weak PyUnicodeUCS4_DecodeUTF8 = __spl_fi_PyUnicode_DecodeUTF8
#pragma weak PyUnicodeUCS4_AsUTF8String = __spl_fi_PyUnicode_AsUTF8String
#pragma weak PyString_AsString = __spl_fi_PyBytes_AsString
#endif

#if PY_MAJOR_VERSION == 3
typedef char * (*__splpy_uauas_fp)(PyObject *, Py_ssize_t);
typedef PyObject * (*__splpy_mvfm_fp)(char *, Py_ssize_t, int);
extern "C" {
  static __splpy_uauas_fp __spl_fp_PyUnicode_AsUTF8AndSize;
  static __splpy_mvfm_fp __spl_fp_PyMemoryView_FromMemory;
  static char * __spl_fi_PyUnicode_AsUTF8AndSize(PyObject * o, Py_ssize_t size) {
     return __spl_fp_PyUnicode_AsUTF8AndSize(o, size);
  }
  static PyObject * __spl_fi_PyMemoryView_FromMemory(char *mem, Py_ssize_t size, int flags) {
     return __spl_fp_PyMemoryView_FromMemory(mem, size, flags);
  }
}
#pragma weak PyUnicode_AsUTF8AndSize = __spl_fi_PyUnicode_AsUTF8AndSize
#pragma weak PyMemoryView_FromMemory = __spl_fi_PyMemoryView_FromMemory

#else
typedef int (*__splpy_sasas_fp)(PyObject *, char **, Py_ssize_t *);
typedef PyObject * (*__splpy_mvfb_fp)(Py_buffer *);
typedef int (*__splpy_bfi_fp)(Py_buffer *, PyObject *, void *, Py_ssize_t, int, int);
extern "C" {
  static __splpy_sasas_fp __spl_fp_PyString_AsStringAndSize;
  static __splpy_mvfb_fp __spl_fp_PyMemoryView_FromBuffer;
  static __splpy_bfi_fp __spl_fp_PyBuffer_FillInfo;
  static int __spl_fi_PyString_AsStringAndSize(PyObject * o, char ** buf, Py_ssize_t *size) {
     return __spl_fp_PyString_AsStringAndSize(o, buf, size);
  }
  static PyObject * __spl_fi_PyMemoryView_FromBuffer(Py_buffer *buf) {
     return __spl_fp_PyMemoryView_FromBuffer(buf);
  }
  static int __spl_fi_PyBuffer_FillInfo(Py_buffer *view, PyObject *o, void *buf, Py_ssize_t len, int readonly, int flags) {
     return __spl_fp_PyBuffer_FillInfo(view, o, buf, len, readonly, flags);
  }
}
#pragma weak PyString_AsStringAndSize = __spl_fi_PyString_AsStringAndSize
#pragma weak PyMemoryView_FromBuffer = __spl_fi_PyMemoryView_FromBuffer
#pragma weak PyBuffer_FillInfo = __spl_fi_PyBuffer_FillInfo
#endif

/*
 * Loading modules, running code
 */

typedef PyObject* (*__splpy_ogas_fp)(PyObject *, const char *);
typedef int (*__splpy_rssf_fp)(const char *, PyCompilerFlags *);
#if __SPLPY_EC_MODULE_OK
typedef PyObject* (*__splpy_mc2_fp)(PyModuleDef *, int);
typedef int (*__splpy_sam_fp)(PyObject *, PyModuleDef *);
#endif

extern "C" {
  static __splpy_ogas_fp __spl_fp_PyObject_GetAttrString;
  static __splpy_rssf_fp __spl_fp_PyRun_SimpleStringFlags;
  static __splpy_p_ppp_fp __spl_fp_PyObject_Call;
  static __splpy_p_pp_fp __spl_fp_PyObject_CallObject;
  static __splpy_i_p_fp __spl_fp_PyCallable_Check;
  static __splpy_p_p_fp __spl_fp_PyImport_Import;

#if __SPLPY_EC_MODULE_OK
  static __splpy_mc2_fp __spl_fp_PyModule_Create2;
  static __splpy_sam_fp __spl_fp_PyState_AddModule;
#endif

  static PyObject * __spl_fi_PyObject_GetAttrString(PyObject *o, const char * attr_name) {
     return __spl_fp_PyObject_GetAttrString(o, attr_name);
  }
  static int __spl_fi_PyRun_SimpleStringFlags(const char * command, PyCompilerFlags *flags) {
     return __spl_fp_PyRun_SimpleStringFlags(command, flags);
  }
  static PyObject * __spl_fi_PyObject_Call(PyObject *callable, PyObject * args, PyObject * kwargs) {
     return __spl_fp_PyObject_Call(callable, args, kwargs);
  }
  static PyObject * __spl_fi_PyObject_CallObject(PyObject *callable, PyObject * args) {
     return __spl_fp_PyObject_CallObject(callable, args);
  }
  static int __spl_fi_PyCallable_Check(PyObject *o) {
     return __spl_fp_PyCallable_Check(o);
  }
  static PyObject * __spl_fi_PyImport_Import(PyObject *name) {
     return __spl_fp_PyImport_Import(name);
  }

#if __SPLPY_EC_MODULE_OK
  static PyObject * __spl_fi_PyModule_Create2(PyModuleDef *module, int apivers) {
     return __spl_fp_PyModule_Create2(module, apivers);
  }
  static int __spl_fi_PyState_AddModule(PyObject *module, PyModuleDef *def) {
     return __spl_fp_PyState_AddModule(module, def);
  }
#endif
}
#pragma weak PyObject_GetAttrString = __spl_fi_PyObject_GetAttrString
#pragma weak PyRun_SimpleStringFlags = __spl_fi_PyRun_SimpleStringFlags
#pragma weak PyObject_Call = __spl_fi_PyObject_Call
#pragma weak PyObject_CallObject = __spl_fi_PyObject_CallObject
#pragma weak PyCallable_Check = __spl_fi_PyCallable_Check
#pragma weak PyImport_Import = __spl_fi_PyImport_Import

#if __SPLPY_EC_MODULE_OK
#pragma weak PyModule_Create2 = __spl_fi_PyModule_Create2
#pragma weak PyState_AddModule = __spl_fi_PyState_AddModule
#endif

/*
 * Container Objects
 */

typedef int (*__splpy_dn_fp)(PyObject *, Py_ssize_t *, PyObject **, PyObject **);

extern "C" {
  static __splpy_p_s_fp __spl_fp_PyTuple_New;
  static __splpy_p_p_fp __spl_fp_PyIter_Next;
  static __splpy_v_p_fp __spl_fp_PyDict_New;
  static __splpy_i_ppp_fp __spl_fp_PyDict_SetItem;
  static __splpy_dn_fp __spl_fp_PyDict_Next;
  static __splpy_p_s_fp __spl_fp_PyList_New;
  static __splpy_s_p_fp __spl_fp_PyList_Size;
  static __splpy_p_p_fp __spl_fp_PySet_New;
  static __splpy_s_p_fp __spl_fp_PySet_Size;
  static __splpy_i_pp_fp __spl_fp_PySet_Add;
  static __splpy_p_p_fp __spl_fp_PyObject_GetIter;

  static PyObject * __spl_fi_PyTuple_New(Py_ssize_t size) {
     return __spl_fp_PyTuple_New(size);
  }
  static PyObject * __spl_fi_PyIter_Next(PyObject * o) {
     return __spl_fp_PyIter_Next(o);
  }
  static PyObject * __spl_fi_PyDict_New() {
     return __spl_fp_PyDict_New();
  }
  static int __spl_fi_PyDict_SetItem(PyObject *d, PyObject *k, PyObject *v) {
     return __spl_fp_PyDict_SetItem(d, k, v);
  }
  static int __spl_fi_PyDict_Next(PyObject *d, Py_ssize_t *o,PyObject **k, PyObject **v) {
     return __spl_fp_PyDict_Next(d, o, k, v);
  }
  static PyObject * __spl_fi_PyList_New(Py_ssize_t size) {
     return __spl_fp_PyList_New(size);
  }
  static Py_ssize_t __spl_fi_PyList_Size(PyObject *l) {
     return __spl_fp_PyList_Size(l);
  }
  static PyObject * __spl_fi_PySet_New(PyObject *o) {
     return __spl_fp_PySet_New(o);
  }
  static Py_ssize_t __spl_fi_PySet_Size(PyObject *s) {
     return __spl_fp_PySet_Size(s);
  }
  static int __spl_fi_PySet_Add(PyObject *s, PyObject *v) {
     return __spl_fp_PySet_Add(s, v);
  }
  static PyObject * __spl_fi_PyObject_GetIter(PyObject *o) {
     return __spl_fp_PyObject_GetIter(o);
  }
}
#pragma weak PyTuple_New = __spl_fi_PyTuple_New
#pragma weak PyIter_Next = __spl_fi_PyIter_Next
#pragma weak PyDict_New = __spl_fi_PyDict_New
#pragma weak PyDict_SetItem = __spl_fi_PyDict_SetItem
#pragma weak PyDict_Next = __spl_fi_PyDict_Next
#pragma weak PyList_New = __spl_fi_PyList_New
#pragma weak PyList_Size = __spl_fi_PyList_Size
#pragma weak PySet_New = __spl_fi_PySet_New
#pragma weak PySet_Size = __spl_fi_PySet_Size
#pragma weak PySet_Add = __spl_fi_PySet_Add
#pragma weak PyObject_GetIter = __spl_fi_PyObject_GetIter

/*
 * Type conversion
 */

typedef PyObject * (*__splpy_cfd_fp)(double, double);
typedef unsigned long (*__splpy_laul_fp)(PyObject *);
typedef PyObject * (*__splpy_lful_fp)(unsigned long);
typedef PyObject * (*__splpy_bfl_fp)(long);
typedef PyObject * (*__splpy_lfvp_fp)(void *);
typedef void * (*__splpy_lavp_fp)(PyObject *);

extern "C" {
  static __splpy_i_p_fp __spl_fp_PyObject_IsTrue;
  static __splpy_l_p_fp __spl_fp_PyLong_AsLong;
  static __splpy_p_l_fp __spl_fp_PyLong_FromLong;
  static __splpy_laul_fp __spl_fp_PyLong_AsUnsignedLong;
  static __splpy_lful_fp __spl_fp_PyLong_FromUnsignedLong;
  static __splpy_cfd_fp __spl_fp_PyComplex_FromDoubles;
  static __splpy_p_d_fp __spl_fp_PyFloat_FromDouble;
  static __splpy_d_p_fp __spl_fp_PyFloat_AsDouble;
  static __splpy_d_p_fp __spl_fp_PyComplex_RealAsDouble;
  static __splpy_d_p_fp __spl_fp_PyComplex_ImagAsDouble;
  static __splpy_p_l_fp __spl_fp_PyBool_FromLong;
  static __splpy_lfvp_fp __spl_fp_PyLong_FromVoidPtr;
  static __splpy_lavp_fp __spl_fp_PyLong_AsVoidPtr;

  static int __spl_fi_PyObject_IsTrue(PyObject *o) {
     return __spl_fp_PyObject_IsTrue(o);
  }
  static long __spl_fi_PyLong_AsLong(PyObject *o) {
     return __spl_fp_PyLong_AsLong(o);
  }
  static PyObject * __spl_fi_PyLong_FromLong(long l) {
     return __spl_fp_PyLong_FromLong(l);
  }
  static unsigned long __spl_fi_PyLong_AsUnsignedLong(PyObject *o) {
     return __spl_fp_PyLong_AsUnsignedLong(o);
  }
  static PyObject * __spl_fi_PyLong_FromUnsignedLong(unsigned long l) {
     return __spl_fp_PyLong_FromUnsignedLong(l);
  }
  static PyObject * __spl_fi_PyComplex_FromDoubles(double r, double i) {
     return __spl_fp_PyComplex_FromDoubles(r, i);
  }
  static PyObject * __spl_fi_PyFloat_FromDouble(double d) {
     return __spl_fp_PyFloat_FromDouble(d);
  }
  static double __spl_fi_PyFloat_AsDouble(PyObject *o) {
     return __spl_fp_PyFloat_AsDouble(o);
  }
  static double __spl_fi_PyComplex_RealAsDouble(PyObject *o) {
     return __spl_fp_PyComplex_RealAsDouble(o);
  }
  static double __spl_fi_PyComplex_ImagAsDouble(PyObject *o) {
     return __spl_fp_PyComplex_ImagAsDouble(o);
  }
  static PyObject * __spl_fi_PyBool_FromLong(long l) {
     return __spl_fp_PyBool_FromLong(l);
  }
  static PyObject * __spl_fi_PyLong_FromVoidPtr(void *p) {
     return __spl_fp_PyLong_FromVoidPtr(p);
  }
  static void * __spl_fi_PyLong_AsVoidPtr(PyObject *p) {
     return __spl_fp_PyLong_AsVoidPtr(p);
  }
}
#pragma weak PyObject_IsTrue = __spl_fi_PyObject_IsTrue
#pragma weak PyLong_AsLong = __spl_fi_PyLong_AsLong
#pragma weak PyLong_FromLong = __spl_fi_PyLong_FromLong
#pragma weak PyLong_AsUnsignedLong = __spl_fi_PyLong_AsUnsignedLong
#pragma weak PyLong_FromUnsignedLong = __spl_fi_PyLong_FromUnsignedLong
#pragma weak PyComplex_FromDoubles = __spl_fi_PyComplex_FromDoubles
#pragma weak PyFloat_FromDouble = __spl_fi_PyFloat_FromDouble
#pragma weak PyFloat_AsDouble = __spl_fi_PyFloat_AsDouble
#pragma weak PyComplex_RealAsDouble = __spl_fi_PyComplex_RealAsDouble
#pragma weak PyComplex_ImagAsDouble = __spl_fi_PyComplex_ImagAsDouble
#pragma weak PyBool_FromLong = __spl_fi_PyBool_FromLong
#pragma weak PyLong_FromVoidPtr = __spl_fi_PyLong_FromVoidPtr
#pragma weak PyLong_AsVoidPtr = __spl_fi_PyLong_AsVoidPtr

/*
 * Err Objects
 */
typedef void (*__splpy_ef_fp)(PyObject **, PyObject **, PyObject **);
typedef void (*__splpy_er_fp)(PyObject *, PyObject *, PyObject *);
typedef PyObject * (*__splpy_eo_fp)(void);
extern "C" {
  static __splpy_ef_fp __spl_fp_PyErr_Fetch;
  static __splpy_ef_fp __spl_fp_PyErr_NormalizeException;
  static __splpy_er_fp __spl_fp_PyErr_Restore;
  static __splpy_eo_fp __spl_fp_PyErr_Occurred;
  static __splpy_v_v_fp __spl_fp_PyErr_Print;
  static __splpy_v_v_fp __spl_fp_PyErr_Clear;

  static void __spl_fi_PyErr_Fetch(PyObject **t, PyObject **v, PyObject **tb) {
     __spl_fp_PyErr_Fetch(t,v,tb);
  }
  static void __spl_fi_PyErr_NormalizeException(PyObject **t, PyObject **v, PyObject **tb) {
     __spl_fp_PyErr_NormalizeException(t,v,tb);
  }
  static void __spl_fi_PyErr_Restore(PyObject *t, PyObject *v, PyObject *tb) {
     __spl_fp_PyErr_Restore(t,v,tb);
  }
  static PyObject * __spl_fi_PyErr_Occurred() {
     return __spl_fp_PyErr_Occurred();
  }
  static void  __spl_fi_PyErr_Print() {
     __spl_fp_PyErr_Print();
  }
  static void  __spl_fi_PyErr_Clear() {
     __spl_fp_PyErr_Clear();
  }
}
#pragma weak PyErr_Fetch = __spl_fi_PyErr_Fetch
#pragma weak PyErr_NormalizeException = __spl_fi_PyErr_NormalizeException
#pragma weak PyErr_Restore = __spl_fi_PyErr_Restore
#pragma weak PyErr_Occurred = __spl_fi_PyErr_Occurred
#pragma weak PyErr_Print = __spl_fi_PyErr_Print
#pragma weak PyErr_Clear = __spl_fi_PyErr_Clear


#define __SPLFIX_EX(_CPPNAME, _NAME, _TYPE) \
     { \
     void * sym = dlsym(pydl, _NAME ); \
     if (sym == NULL) \
         throw std::invalid_argument("Python symbol not found: " _NAME); \
     _CPPNAME = ( _TYPE ) sym; \
     }

#define __SPLFIX(_NAME, _TYPE) __SPLFIX_EX( __spl_fp_##_NAME, #_NAME, _TYPE ) 

namespace streamsx {
  namespace topology {

class SplpySym {
  public:
   static void fixSymbols(void * pydl) {

     __SPLFIX(PyGILState_Ensure, __splpy_gil_v_fp);
     __SPLFIX(PyGILState_Release, __splpy_v_gil_fp);

     __SPLFIX(PyObject_Str, __splpy_p_p_fp);

#if PY_MAJOR_VERSION == 3
     __SPLFIX(PyUnicode_DecodeUTF8, __splpy_udu_fp);
     __SPLFIX(PyUnicode_AsUTF8String, __splpy_uaus_fp);
     __SPLFIX(PyBytes_AsString, __splpy_c_p_fp);
#else
     __SPLFIX_EX(__spl_fp_PyUnicode_DecodeUTF8, "PyUnicodeUCS4_DecodeUTF8", __splpy_udu_fp);
     __SPLFIX_EX(__spl_fp_PyUnicode_AsUTF8String, "PyUnicodeUCS4_AsUTF8String", __splpy_uaus_fp);
     __SPLFIX_EX(__spl_fp_PyBytes_AsString, "PyString_AsString", __splpy_c_p_fp);
#endif

#if PY_MAJOR_VERSION == 3
     __SPLFIX(PyUnicode_AsUTF8AndSize, __splpy_uauas_fp);
     __SPLFIX(PyMemoryView_FromMemory, __splpy_mvfm_fp);
#else
     __SPLFIX(PyString_AsStringAndSize, __splpy_sasas_fp);
     __SPLFIX(PyMemoryView_FromBuffer, __splpy_mvfb_fp);
     __SPLFIX(PyBuffer_FillInfo, __splpy_bfi_fp);
#endif

     __SPLFIX(PyObject_GetAttrString, __splpy_ogas_fp);
     __SPLFIX(PyRun_SimpleStringFlags, __splpy_rssf_fp);
     __SPLFIX(PyObject_Call, __splpy_p_ppp_fp);
     __SPLFIX(PyObject_CallObject, __splpy_p_pp_fp);
     __SPLFIX(PyCallable_Check, __splpy_i_p_fp);
     __SPLFIX(PyImport_Import, __splpy_p_p_fp);

#if __SPLPY_EC_MODULE_OK
     __SPLFIX(PyModule_Create2, __splpy_mc2_fp);
     __SPLFIX(PyState_AddModule, __splpy_sam_fp);
#endif
 
     __SPLFIX(PyTuple_New, __splpy_p_s_fp);
     __SPLFIX(PyIter_Next, __splpy_p_p_fp);
     __SPLFIX(PyDict_New, __splpy_v_p_fp);
     __SPLFIX(PyDict_SetItem, __splpy_i_ppp_fp);
     __SPLFIX(PyDict_Next, __splpy_dn_fp);
     __SPLFIX(PyList_New, __splpy_p_s_fp);
     __SPLFIX(PyList_Size, __splpy_s_p_fp);
     __SPLFIX(PySet_New, __splpy_p_p_fp);
     __SPLFIX(PySet_Size, __splpy_s_p_fp);
     __SPLFIX(PySet_Add, __splpy_i_pp_fp);
     __SPLFIX(PyObject_GetIter, __splpy_p_p_fp);

     __SPLFIX(PyObject_IsTrue, __splpy_i_p_fp);
     __SPLFIX(PyLong_AsLong, __splpy_l_p_fp);
     __SPLFIX(PyLong_FromLong, __splpy_p_l_fp);
     __SPLFIX(PyLong_AsUnsignedLong, __splpy_laul_fp);
     __SPLFIX(PyLong_FromUnsignedLong, __splpy_lful_fp);
     __SPLFIX(PyComplex_FromDoubles, __splpy_cfd_fp);
     __SPLFIX(PyFloat_FromDouble, __splpy_p_d_fp);
     __SPLFIX(PyFloat_AsDouble, __splpy_d_p_fp);
     __SPLFIX(PyComplex_RealAsDouble, __splpy_d_p_fp);
     __SPLFIX(PyComplex_ImagAsDouble, __splpy_d_p_fp);
     __SPLFIX(PyBool_FromLong, __splpy_p_l_fp);
     __SPLFIX(PyLong_FromVoidPtr, __splpy_lfvp_fp);
     __SPLFIX(PyLong_AsVoidPtr, __splpy_lavp_fp);

     __SPLFIX(PyErr_Fetch, __splpy_ef_fp);
     __SPLFIX(PyErr_NormalizeException, __splpy_ef_fp);
     __SPLFIX(PyErr_Restore, __splpy_er_fp);
     __SPLFIX(PyErr_Occurred, __splpy_eo_fp);
     __SPLFIX(PyErr_Print, __splpy_v_v_fp);
     __SPLFIX(PyErr_Clear, __splpy_v_v_fp);
   }
};

}}

#endif

