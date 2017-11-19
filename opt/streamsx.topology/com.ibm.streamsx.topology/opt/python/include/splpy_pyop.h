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
 * Functionality related to Python decorated SPL operators
 */

#ifndef __SPL__SPLPY_PYOP_H
#define __SPL__SPLPY_PYOP_H

#include "splpy_general.h"
#include "splpy_setup.h"
#include "splpy_op.h"

namespace streamsx {
  namespace topology {

class SplpyPyOp : public SplpyOp {
  public:
      SplpyPyOp(SPL::Operator * op) :
         SplpyOp(op, "/opt/.__splpy/common") {
      }
};

}}

#endif

