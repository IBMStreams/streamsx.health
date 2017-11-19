# SPL_CGT_INCLUDE: ../pyspltuple.cgt
# SPL_CGT_INCLUDE: ../pyspltuple2dict.cgt
# SPL_CGT_INCLUDE: ../pywrapfunction.cgt

package PyFunctionTransform_cpp;
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
    my $pyoutstyle = splpy_tuplestyle($model->getOutputPortAt(0));
   print "\n";
   print "\n";
   print '// Constructor', "\n";
   print 'MY_OPERATOR_SCOPE::MY_OPERATOR::MY_OPERATOR() :', "\n";
   print '   function_(NULL),', "\n";
   print '   pyInNames_(NULL)', "\n";
   print '{', "\n";
   print '  std::string tkDir = ProcessingElement::pe().getToolkitDirectory();', "\n";
   print '  std::string streamsxDir = tkDir + "/opt/python/packages/streamsx/topology";', "\n";
   print '  std::string splpySetup = streamsxDir + "/splpy_setup.py";', "\n";
   print '  const char* spl_setup_py = splpySetup.c_str();', "\n";
   print "\n";
   print '  streamsx::topology::Splpy::loadCPython(spl_setup_py);', "\n";
   print "\n";
   print '  streamsx::topology::PyGILLock lock;', "\n";
   print "\n";
   print '    PyObject *_module_;', "\n";
   print '    PyObject *_function_;', "\n";
   print "\n";
   print '    std::string appDirSetup = "import streamsx.topology.runtime\\n";', "\n";
   print '    appDirSetup += "streamsx.topology.runtime.setupOperator(\\"";', "\n";
   print '    appDirSetup += ';
   print $model->getParameterByName("toolkitDir")->getValueAt(0)->getCppExpression();
   print ';', "\n";
   print '    appDirSetup += "\\")\\n";', "\n";
   print "\n";
   print '    const char* spl_setup_appdir = appDirSetup.c_str();', "\n";
   print '    if (PyRun_SimpleString(spl_setup_appdir) != 0) {', "\n";
   print '         SPLAPPTRC(L_ERROR, "Python script splpy_setup.py failed!", "python");', "\n";
   print '         throw streamsx::topology::pythonException("splpy_setup.py");', "\n";
   print '    }', "\n";
   print "\n";
    # Select the Python wrapper function
   
    my $pywrapfunc= $pystyle . '_in__' . $pyoutstyle . '_out';
    
   print "\n";
   print "\n";
    # setup the function that will be called to process
    # each tuple. the Perl variable $pywrapfunc must
    # be set to the name of the setup Python function
    # that will be called to wrap the user's function.
   
    my $pyModule =  $model->getParameterByName("pyModule")->getValueAt(0)->getCppExpression() . '.c_str()';
    my $pyCallableName = $model->getParameterByName("pyName")->getValueAt(0)->getCppExpression() . '.c_str()';
    my $pyCallable = $model->getParameterByName("pyCallable");
    $pyCallable = $pyCallable->getValueAt(0)->getCppExpression() . '.c_str()' if $pyCallable;
   print "\n";
   print "\n";
   print '    // pointer to the application function or callable class', "\n";
   print '    PyObject * appCallable = ', "\n";
   print '      streamsx::topology::Splpy::loadFunction(';
   print $pyModule;
   print ', ';
   print $pyCallableName;
   print ');', "\n";
   print "\n";
   print '    // The object to be called is either appCallable for', "\n";
   print '    // a function passed into the operator', "\n";
   print '    // or a pickled encoded class instance', "\n";
   print '    // represented as a string in parameter pyCallable', "\n";
   print '    ', "\n";
   print '    ';
    if ($pyCallable) { 
   print "\n";
   print '      // argument is the serialized callable instance', "\n";
   print '      Py_DECREF(appCallable);', "\n";
   print '      appCallable = Py_BuildValue("s", ';
   print $pyCallable;
   print ');', "\n";
   print '    ';
   }
   print "\n";
   print "\n";
   print '     PyObject * depickleInput = streamsx::topology::Splpy::loadFunction("streamsx.topology.runtime", "';
   print $pywrapfunc;
   print '");', "\n";
   print '    PyObject * funcArg = PyTuple_New(1);', "\n";
   print '    PyTuple_SetItem(funcArg, 0, appCallable);', "\n";
   print '    function_ = PyObject_CallObject(depickleInput, funcArg);', "\n";
   print '    Py_DECREF(depickleInput);', "\n";
   print '    Py_DECREF(funcArg);', "\n";
   print '    if(function_ == 0){', "\n";
   print '      streamsx::topology::flush_PyErr_Print();', "\n";
   print '      throw;', "\n";
   print '    }', "\n";
   print "\n";
    if ($pystyle eq 'dict') { 
   print "\n";
   print '     pyInNames_ = streamsx::topology::Splpy::pyAttributeNames(', "\n";
   print '               getInputPortAt(0));', "\n";
    } 
   print "\n";
   print '}', "\n";
   print "\n";
   print '// Destructor', "\n";
   print 'MY_OPERATOR_SCOPE::MY_OPERATOR::~MY_OPERATOR() ', "\n";
   print '{', "\n";
   print '  streamsx::topology::PyGILLock lock;', "\n";
   print '  if (function_)', "\n";
   print '    Py_DECREF(function_);', "\n";
   print '  if (pyInNames_)', "\n";
   print '    Py_DECREF(pyInNames_);', "\n";
   print '}', "\n";
   print "\n";
   print '// Notify pending shutdown', "\n";
   print 'void MY_OPERATOR_SCOPE::MY_OPERATOR::prepareToShutdown() ', "\n";
   print '{', "\n";
   print '    streamsx::topology::PyGILLock lock;', "\n";
   print '    streamsx::topology::flush_PyErrPyOut();', "\n";
   print '}', "\n";
   print "\n";
   print '// Tuple processing for non-mutating ports', "\n";
   print 'void MY_OPERATOR_SCOPE::MY_OPERATOR::process(Tuple const & tuple, uint32_t port)', "\n";
   print '{', "\n";
   print '  IPort0Type const &ip = static_cast<IPort0Type const &>(tuple);', "\n";
   print "\n";
   print splpy_inputtuple2value($pystyle, $pyoutstyle);
   print "\n";
   print "\n";
   if ($pystyle eq 'dict') {
   print "\n";
   print "\n";
   print '// process the attributes in the spl tuple', "\n";
   print '// into a python dictionary object', "\n";
   print '  PyObject *value = 0;', "\n";
   print '  {', "\n";
   print '  streamsx::topology::PyGILLock lockdict;', "\n";
   print '  PyObject * pyDict = PyDict_New();', "\n";
        for (my $i = 0; $i < $pynumattrs; ++$i) {
            print convertAndAddToPythonDictionaryObject("ip", $i, $pyatypes[$i], $pyanames[$i], 'pyInNames_');
        }
   print "\n";
   print '  value = pyDict;', "\n";
   print '  }', "\n";
   }
   print "\n";
   print '  OPort0Type otuple;', "\n";
   print '  if (streamsx::topology::Splpy::pyTupleTransform(function_, value,', "\n";
   print '       otuple.get_';
   print $model->getOutputPortAt(0)->getAttributeAt(0)->getName();
   print '()))', "\n";
   print '     submit(otuple, 0);', "\n";
   print '}', "\n";
   print "\n";
   SPL::CodeGen::implementationEpilogue($model);
   print "\n";
   CORE::exit $SPL::CodeGen::USER_ERROR if ($SPL::CodeGen::sawError);
}
1;
