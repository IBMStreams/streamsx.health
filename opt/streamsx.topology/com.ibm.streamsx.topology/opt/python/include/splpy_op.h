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
 * Functionality related to operators
 */

#ifndef __SPL__SPLPY_OP_H
#define __SPL__SPLPY_OP_H

#include "splpy_general.h"
#include "splpy_ec_api.h"

namespace streamsx {
  namespace topology {

class SplpyOp {
  public:
      SplpyOp(SPL::Operator *op, const char * spl_setup_py) :
          op_(op),
          callable_(NULL),
          pydl_(NULL)

#if __SPLPY_EC_MODULE_OK
          , opc_(NULL)
#endif

      {
          pydl_ = SplpySetup::loadCPython(spl_setup_py);

          SplpyGIL lock;
          SPL::rstring outDir(op->getPE().getOutputDirectory());
          PyObject * pyOutDir = pySplValueToPyObject(outDir);
          SplpyGeneral::callVoidFunction(
               "streamsx.topology.runtime", "add_output_packages", pyOutDir, NULL);
#if __SPLPY_EC_MODULE_OK
          opc_ = PyLong_FromVoidPtr((void*)op);
          if (opc_ == NULL)
              throw SplpyGeneral::pythonException("capsule");
#endif
      }

      ~SplpyOp()
      {
        {
          SplpyGIL lock;

          if (callable_ != NULL)
              Py_DECREF(callable_);

#if __SPLPY_EC_MODULE_OK
          if (opc_ != NULL)
              Py_DECREF(opc_);
#endif
        }
        if (pydl_ != NULL)
          (void) dlclose(pydl_);
      }

      SPL::Operator * op() {
         return op_;
      }

      void setCallable(PyObject * callable) {
           callable_ = callable;
      }
      PyObject * callable() {
          return callable_;
      }

      /**
       * Actions for a Python operator on prepareToShutdown
       * Flush any pending output.
      */
      void prepareToShutdown() {
          SplpyGIL lock;
          if (callable_) {
             // Call _shutdown_op which will invoke
             // __exit__ on the users object if
             // it's a class instance and has
             // __enter__ and __exit__
             // callVoid steals the reference to callable_
             Py_INCREF(callable_);
             SplpyGeneral::callVoidFunction(
               "streamsx.ec", "_shutdown_op", callable_, NULL);
          }
          SplpyGeneral::flush_PyErrPyOut();
      }

#if __SPLPY_EC_MODULE_OK
      // Get the capture with a new ref
      PyObject * opc() {
         Py_INCREF(opc_);
         return opc_;
      }
     
      // Set the operator capsule as a Python thread local
      // use streamsx.ec._set_opc so that it is availble
      // through the operator's class __init__ function.
      void setopc() {
         SplpyGeneral::callVoidFunction(
               "streamsx.ec", "_set_opc", opc(), NULL);
      }

      // Clear the thread local for the operator capsule
      void clearopc() {
          SplpyGeneral::callVoidFunction(
               "streamsx.ec", "_clear_opc",
               NULL, NULL);
      }
#endif

   private:
      SPL::Operator *op_;
 
      // Python object used to process tuples
      PyObject *callable_;

      // Handle to libpythonX.Y.so
      void * pydl_;

#if __SPLPY_EC_MODULE_OK
      // PyLong of op_
      PyObject *opc_;
#endif
};

}}

#endif

