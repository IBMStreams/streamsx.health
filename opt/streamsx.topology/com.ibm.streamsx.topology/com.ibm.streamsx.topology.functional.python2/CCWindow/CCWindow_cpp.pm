# SPL_CGT_INCLUDE: ../../opt/python/codegen/py_pyTupleTosplTuple.cgt
# SPL_CGT_INCLUDE: ../pyspltuple.cgt
# SPL_CGT_INCLUDE: ../../opt/python/codegen/py_splTupleCheckForBlobs.cgt
# SPL_CGT_INCLUDE: ../pyspltuple2dict.cgt

package CCWindow_cpp;
use strict; use Cwd 'realpath';  use File::Basename;  use lib dirname(__FILE__);  use SPL::Operator::Instance::OperatorInstance; use SPL::Operator::Instance::Annotation; use SPL::Operator::Instance::Context; use SPL::Operator::Instance::Expression; use SPL::Operator::Instance::ExpressionTree; use SPL::Operator::Instance::ExpressionTreeEvaluator; use SPL::Operator::Instance::ExpressionTreeVisitor; use SPL::Operator::Instance::ExpressionTreeCppGenVisitor; use SPL::Operator::Instance::InputAttribute; use SPL::Operator::Instance::InputPort; use SPL::Operator::Instance::OutputAttribute; use SPL::Operator::Instance::OutputPort; use SPL::Operator::Instance::Parameter; use SPL::Operator::Instance::StateVariable; use SPL::Operator::Instance::TupleValue; use SPL::Operator::Instance::Window; 
sub main::generate($$) {
   my ($xml, $signature) = @_;  
   print "// $$signature\n";
   my $model = SPL::Operator::Instance::OperatorInstance->new($$xml);
   unshift @INC, dirname ($model->getContext()->getOperatorDirectory()) . "/../impl/nl/include";
   $SPL::CodeGenHelper::verboseMode = $model->getContext()->isVerboseModeOn();
   print '/* Additional includes go here */', "\n";
   print "\n";
   print '#include "splpy.h"', "\n";
   print '#include "splpy_funcop.h"', "\n";
   print "\n";
   print 'using namespace streamsx::topology;', "\n";
   print "\n";
   SPL::CodeGen::implementationPrologue($model);
   print "\n";
   print "\n";
    # Generic setup of a variety of variables to
    # handle conversion of spl tuples to/from Python
   
    my $tkdir = $model->getContext()->getToolkitDirectory();
    my $pydir = $tkdir."/opt/python";
   
    require $pydir."/codegen/splpy.pm";
   
    # setup the variables used when processing spltuples
    my $pyport = $model->getInputPortAt(0);
    my $pytupleType = $pyport->getSPLTupleType();
    my @pyanames = SPL::CodeGen::Type::getAttributeNames($pytupleType);
    my @pyatypes = SPL::CodeGen::Type::getAttributeTypes($pytupleType);
   
    my $pynumattrs = $pyport->getNumberOfAttributes();
    
    my $pytuple = $pyport->getCppTupleName();
   
    # determine which input tuple style is being used
   
    my $pystyle = splpy_tuplestyle($model->getInputPortAt(0));
   print "\n";
   print "\n";
   print "\n";
   print "\n";
    # Select the Python wrapper function
    my $pyoutstyle = splpy_tuplestyle($model->getOutputPortAt(0));
   
    if (($pystyle eq 'dict') || ($pyoutstyle eq 'dict')) {
       SPL::CodeGen::exitln("Dictionary input and output not supported.");
    }
    
    my $in_pywrapfunc =  $pystyle . '_in__object_out';
    my $out_pywrapfunc=  'pickle_in__' . $pyoutstyle . '_out';
   print "\n";
   print "\n";
   print '#define SPLPY_TUPLE_MAP(f, v, r, occ) \\', "\n";
   print '    streamsx::topology::Splpy::pyTupleMap(f, v, r)', "\n";
   print "\n";
   print '// Constructor', "\n";
   print 'MY_OPERATOR_SCOPE::MY_OPERATOR::MY_OPERATOR() :', "\n";
   print '   funcop_(NULL),', "\n";
   print '   pyInNames_(NULL),', "\n";
   print '   occ_(-1),', "\n";
   print '   window_(*this, 0,', "\n";
   print '   CountWindowPolicy(';
   print $model->getParameterByName("evictConfig")->getValueAt(0)->getSPLExpression();
   print '),', "\n";
   print '   CountWindowPolicy(';
   print $model->getParameterByName("triggerConfig")->getValueAt(0)->getSPLExpression();
   print '))', "\n";
   print '{', "\n";
   print '    window_.registerOnWindowTriggerHandler(this);', "\n";
   print '    window_.registerAfterTupleEvictionHandler(this);', "\n";
   print "\n";
   print '    const char * in_wrapfn = "';
   print $in_pywrapfunc;
   print '";', "\n";
   print '    const char * out_wrapfn = "';
   print $out_pywrapfunc;
   print '";', "\n";
   print '    const char * functions_module = "streamsx.topology.functions";', "\n";
   print '    const char * identity_function = "identity";', "\n";
   print "\n";
   # If occ parameter is positive then pass-by-ref is possible
   # Generate code to allow pass by ref but only use when
   # not connected to a PE output port.
   
    my $oc = $model->getParameterByName("outputConnections");
   
    if ($oc) {
       my $occ = $oc->getValueAt(0)->getSPLExpression();
       if ($occ > 0) {
           my $pybyrefwrapfunc = 'pickle_in__object_out';
   print "\n";
   print "\n";
   print '#undef SPLPY_TUPLE_MAP', "\n";
   print '#define SPLPY_TUPLE_MAP(f, v, r, occ) \\', "\n";
   print '    streamsx::topology::Splpy::pyTupleMapByRef(f, v, r, occ)', "\n";
   print "\n";
   print '    if (!this->getOutputPortAt(0).isConnectedToAPEOutputPort()) {', "\n";
   print '       // pass by reference', "\n";
   print '       out_wrapfn = "';
   print $pybyrefwrapfunc;
   print '";', "\n";
   print '       occ_ = ';
   print $occ;
   print ';', "\n";
   print '    }', "\n";
       } 
    }
   print "\n";
   print "\n";
   print '    funcop_ = new SplpyFuncOp(this, out_wrapfn);', "\n";
   print "\n";
   print '    // spl_in_object_out points to the wrapped identity function used to convert incoming tuples to ', "\n";
   print '    // Python objects.', "\n";
   print '    {', "\n";
   print '        SplpyGIL lock;', "\n";
   print '        spl_in_object_out = SplpyGeneral::loadFunction(functions_module, identity_function);', "\n";
   print '        spl_in_object_out = SplpyGeneral::callFunction("streamsx.topology.runtime", in_wrapfn, spl_in_object_out, NULL);', "\n";
   print '    }', "\n";
   print "\n";
    if ($pystyle eq 'dict') { 
   print "\n";
   print '     SplpyGIL lock;', "\n";
   print '     pyInNames_ = streamsx::topology::Splpy::pyAttributeNames(', "\n";
   print '               getInputPortAt(0));', "\n";
    } 
   print "\n";
   print '}', "\n";
   print "\n";
   print '// Destructor', "\n";
   print 'MY_OPERATOR_SCOPE::MY_OPERATOR::~MY_OPERATOR() ', "\n";
   print '{', "\n";
    if ($pystyle eq 'dict') { 
   print "\n";
   print '    if (pyInNames_) {', "\n";
   print '      SplpyGIL lock;', "\n";
   print '      Py_DECREF(pyInNames_);', "\n";
   print '    }', "\n";
    } 
   print "\n";
   print "\n";
   print '  delete funcop_;', "\n";
   print '  {', "\n";
   print '      SplpyGIL lock;', "\n";
   print '      Py_DECREF(spl_in_object_out);', "\n";
   print '  }', "\n";
   print '}', "\n";
   print "\n";
   print '// Notify pending shutdown', "\n";
   print 'void MY_OPERATOR_SCOPE::MY_OPERATOR::prepareToShutdown() ', "\n";
   print '{', "\n";
   print '    funcop_->prepareToShutdown();', "\n";
   print '}', "\n";
   print "\n";
   print '// Tuple processing for non-mutating ports', "\n";
   print 'void MY_OPERATOR_SCOPE::MY_OPERATOR::process(Tuple const & tuple, uint32_t port)', "\n";
   print '{', "\n";
   print '  IPort0Type const &ip = static_cast<IPort0Type const &>(tuple);', "\n";
   print "\n";
   print splpy_inputtuple2value($pystyle);
   print "\n";
   print "\n";
   if ($pystyle eq 'dict') {
   print "\n";
   print "\n";
   print '// process the attributes in the spl tuple', "\n";
   print '// into a python dictionary object', "\n";
   # Fix up names for blobs script
   my $inputAttrs2Py = $pynumattrs;
   my @itypes = @pyatypes;
   print "\n";
      #Check if a blob exists in the input schema
      for (my $i = 0; $i < $inputAttrs2Py; ++$i) {
         if (typeHasBlobs($itypes[$i])) {
   print "\n";
   print '   PYSPL_MEMORY_VIEW_CLEANUP();', "\n";
            last;
         }
      }
   print "\n";
   print "\n";
   print '  PyObject *value = 0;', "\n";
   print '  {', "\n";
   print '  SplpyGIL lockdict;', "\n";
   print '  PyObject * pyDict = PyDict_New();', "\n";
        for (my $i = 0; $i < $pynumattrs; ++$i) {
            print convertAndAddToPythonDictionaryObject("ip", $i, $pyatypes[$i], $pyanames[$i], 'pyInNames_');
        }
   print "\n";
   print '  value = pyDict;', "\n";
   print '  }', "\n";
   }
   print "\n";
   print "\n";
   print '  PyObject *python_value;', "\n";
   print "\n";
   print '// If the input style is pickle,', "\n";
   print "\n";
   print '  // None of the streamsx::topology methods in this scope grab the lock', "\n";
   print '  // so we need to do it here.', "\n";
   print '  ', "\n";
   print '  ';
   if ($pystyle eq 'pickle'){
   print "\n";
   print "\n";
   print '      unsigned char const *data = value.getData();', "\n";
   print '      unsigned char fmt = *data;', "\n";
   print '      if (fmt == STREAMSX_TPP_PTR) {', "\n";
   print '          __SPLTuplePyPtr *stp = (__SPLTuplePyPtr *)(data);', "\n";
   print '          python_value = stp->pyptr;', "\n";
   print '      }', "\n";
   print '      // Anything ASCII is also Pickle (Python 2 default format)', "\n";
   print '      else if (fmt <= STREAMSX_TPP_PICKLE) {', "\n";
   print '      	  // This is a pickled value. Need to depickle it.', "\n";
   print '	  {', "\n";
   print '	      SplpyGIL lock; ', "\n";
   print '              python_value = pySplValueToPyObject(value);', "\n";
   print "\n";
   print '	      // Create a tuple to pass to the depickling wrapper function.	  ', "\n";
   print '	      PyObject * pyTuple;', "\n";
   print '              pyTuple = PyTuple_New(2);', "\n";
   print '              PyTuple_SET_ITEM(pyTuple, 0, python_value);', "\n";
   print '              Py_INCREF(python_value);', "\n";
   print '              PyTuple_SET_ITEM(pyTuple, 1, python_value);', "\n";
   print "\n";
   print '	      // Depickle the tuple.', "\n";
   print '	      python_value = streamsx::topology::SplpyGeneral::pyCallObject(spl_in_object_out, pyTuple);', "\n";
   print '	  } // End SplpyGIL lock', "\n";
   print '      }', "\n";
   print '  ';
   } else {
   print "\n";
   print '      	{', "\n";
   print '	    // If the tuple is not a pickled object, or a reference, convert it to a PyObject', "\n";
   print '	    // using pySplProcessTuple', "\n";
   print '	    SplpyGIL lock;', "\n";
   print '            python_value = streamsx::topology::pySplProcessTuple(spl_in_object_out, value);', "\n";
   print '	} // End SplpyGIL lock', "\n";
   print '  ';
   }
   print "\n";
   print "\n";
   print "\n";
   print '  window_.insert(python_value);', "\n";
   print '}', "\n";
   print "\n";
   print "\n";
   print '// ##############################', "\n";
   print '// Window Event Handler Overrides', "\n";
   print '// ##############################', "\n";
   print "\n";
   print "\n";
   print 'void MY_OPERATOR_SCOPE::MY_OPERATOR::afterTupleEvictionEvent(', "\n";
   print '     Window<PyObject *> & window,  Window<PyObject *>::TupleType & tuple,  Window<PyObject *>::PartitionType const & partition) {', "\n";
   print '     // Drop reference to tuple after it is removed from the window.', "\n";
   print '     SplpyGIL lock;', "\n";
   print '     Py_DECREF(tuple);', "\n";
   print '}', "\n";
   print "\n";
   print 'void MY_OPERATOR_SCOPE::MY_OPERATOR::onWindowTriggerEvent(Window<PyObject *> & window, Window<PyObject *>::PartitionType const & key){    ', "\n";
   print '    Window<PyObject *>::StorageType & storage = window.getWindowStorage();', "\n";
   print "\n";
   print '    Window<PyObject *>::DataType & content = storage[key];', "\n";
   print '    PyObject *items;', "\n";
   print '    {', "\n";
   print '    SplpyGIL lock;', "\n";
   print '    items = PyTuple_New(std::distance(content.begin(), content.end()));', "\n";
   print '    unsigned int idx = 0;', "\n";
   print '    for(WindowType::DataType::iterator it=content.begin(); it!=content.end(); ++it) {', "\n";
   print '        PyObject *item = *it;', "\n";
   print '	// The tuple steals a reference, increment such that the window can maintain a copy', "\n";
   print '	// once the tuple is deleted.', "\n";
   print '	Py_INCREF(item);', "\n";
   print '	PyTuple_SET_ITEM(items, idx, item);', "\n";
   print '	++idx;', "\n";
   print '    }', "\n";
   print '    }', "\n";
   print '    PyObject *value = items;', "\n";
   print '  OPort0Type otuple;', "\n";
   print "\n";
   if ($pyoutstyle eq 'dict') {
   print "\n";
   print '  {', "\n";
   print '  {', "\n";
   print '  SplpyGIL lock;', "\n";
   print "\n";
   print '  PyObject * pyTuple;', "\n";
   print '  pyTuple = PyTuple_New(1);', "\n";
   print '  PyTuple_SET_ITEM(pyTuple, 0, value);', "\n";
   print '  PyObject * ret = streamsx::topology::SplpyGeneral::pyCallObject(funcop_->callable(), pyTuple);', "\n";
   print '  if (streamsx::topology::SplpyGeneral::isNone(ret)){', "\n";
   print '     return;', "\n";
   print '  }', "\n";
   print "\n";
   print '  fromPythonToPort0(ret, otuple);', "\n";
   print '  Py_DECREF(ret);', "\n";
   print '  }', "\n";
   print '  }', "\n";
   print '  ', "\n";
    } else { 
   print "\n";
   print "\n";
   print '  if (SPLPY_TUPLE_MAP(funcop_->callable(), value,', "\n";
   print '       otuple.get_';
   print $model->getOutputPortAt(0)->getAttributeAt(0)->getName();
   print '(), occ_))', "\n";
   print "\n";
   }
   print '{', "\n";
   print '     submit(otuple, 0);', "\n";
   print '     }', "\n";
   print '}', "\n";
   print "\n";
   print '// ##################################', "\n";
   print '// End Window Event Handler Overrides', "\n";
   print '// ##################################', "\n";
   print "\n";
   print "\n";
   if ($pyoutstyle eq 'dict') {
     # In this case we don't want the function that
     # converts the Python tuple to an SPL tuple to
     # copy attributes from the input port
     my $iport;
   
     my $oport = $model->getOutputPortAt(0);
     my $otupleType = $oport->getSPLTupleType();
     my @onames = SPL::CodeGen::Type::getAttributeNames($otupleType);
     my @otypes = SPL::CodeGen::Type::getAttributeTypes($otupleType);
   
   print "\n";
   print '// Create member function that converts Python tuple to SPL tuple', "\n";
   # Generates a function in an operator that converts a Python
   # tuple to an SPL tuple for a given port.
   #
   # $oport must be set on entry to required output port
   # $iport can be set to automatically copy input attributes to
   # output attributes when the Python tuple does not supply a value.
   
     my $itypeparam = "";
     if (defined $iport) {
        $itypeparam = ", " . $iport->getCppTupleType() . " const & ituple";
     }
   print "\n";
   print ' ', "\n";
   print 'void MY_OPERATOR_SCOPE::MY_OPERATOR::fromPythonToPort';
   print $oport->getIndex();
   print '(PyObject *pyTuple, ';
   print $oport->getCppTupleType();
   print ' & otuple ';
   print $itypeparam;
   print ') {', "\n";
   print "\n";
   print '  Py_ssize_t frs = PyTuple_GET_SIZE(pyTuple); ', "\n";
   print '    ', "\n";
     if (defined $iport) {
       print 'bool setAttr = false;';
     }
   
     for (my $ai = 0; $ai < $oport->getNumberOfAttributes(); ++$ai) {
       
       my $attribute = $oport->getAttributeAt($ai);
       my $name = $attribute->getName();
       my $atype = $attribute->getSPLType();
       splToPythonConversionCheck($atype);
       
       if (defined $iport) {
                print 'setAttr = false;';
       }
   print "\n";
   print '    if (';
   print $ai;
   print ' < frs) {', "\n";
   print '         // Value from the Python function', "\n";
   print '         PyObject *pyAttrValue = PyTuple_GET_ITEM(pyTuple, ';
   print $ai;
   print ');', "\n";
   print '         if (!SplpyGeneral::isNone(pyAttrValue)) {', "\n";
   print '                  streamsx::topology::pySplValueFromPyObject(', "\n";
   print '                               otuple.get_';
   print $name;
   print '(), pyAttrValue);', "\n";
       if (defined $iport) {
                print 'setAttr = true;';
       }
   print "\n";
   print '      }', "\n";
   print '   }', "\n";
       if (defined $iport) {
       
       # Only copy attributes across if they match on name and type
       my $matchInputAttr = $iport->getAttributeByName($name);
       if (defined $matchInputAttr) {
          if ($matchInputAttr->getSPLType() eq $attribute->getSPLType()) {
   print "\n";
   print '    if (!setAttr) {', "\n";
   print '      // value from the input attribute', "\n";
   print '      otuple.set_';
   print $name;
   print '(ituple.get_';
   print $name;
   print '());', "\n";
   print '    }', "\n";
         }
       }
      }
   print "\n";
   print '         ', "\n";
   }
    
   print "\n";
   print '}', "\n";
   }
   print "\n";
   print "\n";
   print "\n";
   print 'namespace SPL{', "\n";
   print '    Checkpoint & operator <<(Checkpoint &ostr, const PyObject  & obj){', "\n";
   print '        return ostr;', "\n";
   print '    }', "\n";
   print "\n";
   print '    Checkpoint & operator >>(Checkpoint &ostr, const PyObject  & obj){', "\n";
   print '        return ostr;', "\n";
   print '    }', "\n";
   print "\n";
   print '    ByteBuffer<Checkpoint> & operator<<(ByteBuffer<Checkpoint> & ckpt, PyObject * obj){', "\n";
   print '        return ckpt;', "\n";
   print '    }', "\n";
   print "\n";
   print "\n";
   print '    ByteBuffer<Checkpoint> & operator>>(ByteBuffer<Checkpoint> & ckpt, PyObject * obj){', "\n";
   print '        return ckpt;', "\n";
   print '    }', "\n";
   print "\n";
   print ' }', "\n";
   print "\n";
   print 'std::ostream & operator <<(std::ostream &ostr, const PyObject  & obj){', "\n";
   print '    return ostr;', "\n";
   print '}', "\n";
   print "\n";
   print 'std::ostream & operator >>(std::ostream &ostr, const PyObject  & obj){', "\n";
   print '    return ostr;', "\n";
   print '}', "\n";
   SPL::CodeGen::implementationEpilogue($model);
   print "\n";
   CORE::exit $SPL::CodeGen::USER_ERROR if ($SPL::CodeGen::sawError);
}
1;
