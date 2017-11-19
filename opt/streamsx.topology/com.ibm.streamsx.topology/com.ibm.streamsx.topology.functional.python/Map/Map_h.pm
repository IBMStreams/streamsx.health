# SPL_CGT_INCLUDE: ../pyspltuple.cgt

package Map_h;
use strict; use Cwd 'realpath';  use File::Basename;  use lib dirname(__FILE__);  use SPL::Operator::Instance::OperatorInstance; use SPL::Operator::Instance::Annotation; use SPL::Operator::Instance::Context; use SPL::Operator::Instance::Expression; use SPL::Operator::Instance::ExpressionTree; use SPL::Operator::Instance::ExpressionTreeEvaluator; use SPL::Operator::Instance::ExpressionTreeVisitor; use SPL::Operator::Instance::ExpressionTreeCppGenVisitor; use SPL::Operator::Instance::InputAttribute; use SPL::Operator::Instance::InputPort; use SPL::Operator::Instance::OutputAttribute; use SPL::Operator::Instance::OutputPort; use SPL::Operator::Instance::Parameter; use SPL::Operator::Instance::StateVariable; use SPL::Operator::Instance::TupleValue; use SPL::Operator::Instance::Window; 
sub main::generate($$) {
   my ($xml, $signature) = @_;  
   print "// $$signature\n";
   my $model = SPL::Operator::Instance::OperatorInstance->new($$xml);
   unshift @INC, dirname ($model->getContext()->getOperatorDirectory()) . "/../impl/nl/include";
   $SPL::CodeGenHelper::verboseMode = $model->getContext()->isVerboseModeOn();
   print '/* Additional includes go here */', "\n";
   print '#include "splpy_funcop.h"', "\n";
   print "\n";
   print 'using namespace streamsx::topology;', "\n";
   print "\n";
   SPL::CodeGen::headerPrologue($model);
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
    my $oport = $model->getOutputPortAt(0);
   print "\n";
   print "\n";
   print 'class MY_OPERATOR : public MY_BASE_OPERATOR ', "\n";
   print '{', "\n";
   print 'public:', "\n";
   print '  // Constructor', "\n";
   print '  MY_OPERATOR();', "\n";
   print "\n";
   print '  // Destructor', "\n";
   print '  virtual ~MY_OPERATOR(); ', "\n";
   print "\n";
   print '  // Notify termination', "\n";
   print '  void prepareToShutdown(); ', "\n";
   print "\n";
   print '  // Tuple processing for non-mutating ports', "\n";
   print '  void process(Tuple const & tuple, uint32_t port);', "\n";
   print "\n";
   print 'private:', "\n";
   if ($pyoutstyle eq 'dict') {
   print "\n";
   print '    void fromPythonToPort0(PyObject * pyTuple, ';
   print $oport->getCppTupleType();
   print ' & otuple);', "\n";
   }
   print "\n";
   print "\n";
   print '    // Members', "\n";
   print '    // Control for interaction with Python', "\n";
   print '    SplpyFuncOp *funcop_;', "\n";
   print '    ', "\n";
   print '    // Names of input attributes', "\n";
   print '    PyObject *pyInNames_;', "\n";
   print "\n";
   print '    // Number of output connections when passing by ref', "\n";
   print '    // -1 when cannot pass by ref', "\n";
   print '    int32_t occ_;', "\n";
   print '}; ', "\n";
   print "\n";
   SPL::CodeGen::headerEpilogue($model);
   print "\n";
   print "\n";
   CORE::exit $SPL::CodeGen::USER_ERROR if ($SPL::CodeGen::sawError);
}
1;
