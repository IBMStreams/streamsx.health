# SPL_CGT_INCLUDE: ../pyspltuple.cgt
# SPL_CGT_INCLUDE: ../../opt/python/codegen/py_splTupleCheckForBlobs.cgt
# SPL_CGT_INCLUDE: ../pyspltuple2dict.cgt

package HashAdder_cpp;
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
    # Select the Python wrapper function
    my $pywrapfunc= $pystyle . '_in';
   print "\n";
   print "\n";
   print '// Constructor', "\n";
   print 'MY_OPERATOR_SCOPE::MY_OPERATOR::MY_OPERATOR() :', "\n";
   print '   funcop_(NULL),', "\n";
   print '   pyInNames_(NULL)', "\n";
   print '{', "\n";
   print '    funcop_ = new SplpyFuncOp(this, "';
   print $pywrapfunc;
   print '");', "\n";
    if ($pystyle eq 'dict') { 
   print "\n";
   print '     SplpyGIL lock;', "\n";
   print '     pyInNames_ = streamsx::topology::Splpy::pyAttributeNames(', "\n";
   print '               getInputPortAt(0));', "\n";
    } 
   print "\n";
   print '}', "\n";
   print "\n";
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
   print '    delete funcop_;', "\n";
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
   print "\n";
   print '  OPort0Type otuple;', "\n";
   print '  otuple.assignFrom(ip, false);', "\n";
   print '  otuple.set___spl_hash(streamsx::topology::Splpy::pyTupleHash(funcop_->callable(), value));', "\n";
   print "\n";
   }
   print "\n";
   print "\n";
   if ($pystyle ne 'dict') {
   print "\n";
   print "\n";
   print '  OPort0Type otuple(value,', "\n";
   print '       streamsx::topology::Splpy::pyTupleHash(funcop_->callable(), value));', "\n";
   }
   print "\n";
   print '  // submit tuple', "\n";
   print '  submit(otuple, 0);', "\n";
   print '}', "\n";
   print "\n";
   SPL::CodeGen::implementationEpilogue($model);
   print "\n";
   CORE::exit $SPL::CodeGen::USER_ERROR if ($SPL::CodeGen::sawError);
}
1;
