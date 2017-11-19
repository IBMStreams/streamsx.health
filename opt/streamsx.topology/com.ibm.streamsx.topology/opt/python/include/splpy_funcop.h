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

#ifndef __SPL__SPLPY_FUNCOP_H
#define __SPL__SPLPY_FUNCOP_H

#include "splpy_general.h"
#include "splpy_setup.h"
#include "splpy_op.h"
#include "splpy_ec_api.h"

#include <SPL/Runtime/Operator/ParameterValue.h>

namespace streamsx {
  namespace topology {

class SplpyFuncOp : public SplpyOp {
  public:

      SplpyFuncOp(SPL::Operator * op, const std::string & wrapfn) :
         SplpyOp(op, "/opt/python/packages/streamsx/topology")
      {
         addAppPythonPackages();
         loadAndWrapCallable(wrapfn);
      }

      ~SplpyFuncOp() {
      }



  private:

      int hasParam(const char *name) {
          return op()->getParameterNames().count(name);
      }

      const SPL::rstring & param(const char *name) {
          return op()->getParameterValues(name)[0]->getValue();
      }

      /**
       * Load and wrap the callable that will be invoked
       * by the operator.
      */
      void loadAndWrapCallable(const std::string & wrapfn) {
          SplpyGIL lock;

          // pointer to the application function or callable class
          PyObject * appCallable =
             SplpyGeneral::loadFunction(param("pyModule"), param("pyName"));

          // The object to be called is either appCallable for
          // a function passed into the operator
          // or a pickled encoded class instance
          // represented as a string in parameter pyCallable
    
          if (hasParam("pyCallable")) {
             // argument is the serialized callable instance
             PyObject * appClass = appCallable;
             appCallable = pyUnicode_FromUTF8(param("pyCallable").c_str());
             Py_DECREF(appClass);

#if __SPLPY_EC_MODULE_OK
             setopc();
#endif
          }

          setCallable(SplpyGeneral::callFunction(
               "streamsx.topology.runtime", wrapfn, appCallable, NULL));
      }


      /**
       * Load and wrap the callable that will be invoked
       * by the operator.
      */
      void loadAndWrapCallable(const std::string & wrapfn, const std::string & module,
			       const std::string & function) {
          SplpyGIL lock;

          // pointer to the application function
	  PyObject * appCallable =
             SplpyGeneral::loadFunction(module, function);

          setCallable(SplpyGeneral::callFunction(
               "streamsx.topology.runtime", wrapfn, appCallable, NULL));
      }

      /*
       *  Add any packages in the application directory
       *  to the Python path. The application directory
       *  is passed to each invocation of the functional 
       *  operators as the parameter toolkitDir. The value
       *  passed is the toolkit of the invocation of the operator.
       */
      void addAppPythonPackages() {
          SplpyGIL lock;

          PyObject * tkDir =
            streamsx::topology::pyUnicode_FromUTF8(param("toolkitDir"));

          SplpyGeneral::callVoidFunction(
              "streamsx.topology.runtime", "setupOperator", tkDir, NULL);
      }
};

}}

#endif

