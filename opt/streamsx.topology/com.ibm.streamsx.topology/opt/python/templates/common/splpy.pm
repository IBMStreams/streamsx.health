
# Check if a SPL type is supported for conversion
# to/from a Python value.
#
sub splToPythonConversionCheck{

    my ($type) = @_;

    if (SPL::CodeGen::Type::isList($type)) {
        my $element_type = SPL::CodeGen::Type::getElementType($type);
        splToPythonConversionCheck($element_type);
        return;
    }
    elsif (SPL::CodeGen::Type::isSet($type)) {
        my $element_type = SPL::CodeGen::Type::getElementType($type);
        # Python sets must have hashable keys
        # (which excludes Python collection type such as list,map,set)
        # so for now restrict to primitive types)
        #
        # blob is excluded as the value can become
        # invalid while in a set which will likely break
        if (SPL::CodeGen::Type::isPrimitive($element_type)
          && ! SPL::CodeGen::Type::isBlob($element_type)) {
            splToPythonConversionCheck($element_type);
            return;
        }
    }
    elsif (SPL::CodeGen::Type::isMap($type)) {
        my $key_type = SPL::CodeGen::Type::getKeyType($type);
        # Python maps must have hashable keys
        # (which excludes Python collection type such as list,map,set)
        # so for now restrict to primitive types)
        #
        # blob is excluded as the value can become
        # invalid while in a map which will likely break as a key
        if (SPL::CodeGen::Type::isPrimitive($key_type)
          && ! SPL::CodeGen::Type::isBlob($key_type)) {
            splToPythonConversionCheck($key_type);

           my $value_type = SPL::CodeGen::Type::getValueType($type);
           splToPythonConversionCheck($value_type);
           return;
        }
    }
    elsif(SPL::CodeGen::Type::isSigned($type)) {
      return;
    } 
    elsif(SPL::CodeGen::Type::isUnsigned($type)) {
      return;
    } 
    elsif(SPL::CodeGen::Type::isFloat($type)) {
      return;
    } 
    elsif(SPL::CodeGen::Type::isDecimal($type)) {
      return;
    } 
    elsif (SPL::CodeGen::Type::isRString($type) || SPL::CodeGen::Type::isBString($type)) {
      return;
    } 
    elsif (SPL::CodeGen::Type::isUString($type)) {
      return;
    } 
    elsif(SPL::CodeGen::Type::isBoolean($type)) {
      return;
    } 
    elsif(SPL::CodeGen::Type::isTimestamp($type)) {
      return;
    }
    elsif(SPL::CodeGen::Type::isBlob($type)) {
      return;
    }
    elsif (SPL::CodeGen::Type::isComplex32($type) || SPL::CodeGen::Type::isComplex64($type)) {
      return;
    }

    SPL::CodeGen::errorln("SPL type: " . $type . " is not supported for conversion to or from Python."); 
}

#
# Return a C++ expression converting a input attribute
# from an SPL input tuple to a Python object
#
sub convertAttributeToPythonValue {
  my $ituple = $_[0];
  my $type = $_[1];
  my $name = $_[2];

  # input value
  my $iv = $ituple . ".get_" . $name . "()";

  return convertToPythonValueFromExpr($type, $iv);
}

##
## Convert to a Python value from an expression
## Returns a string with a C++ expression
## representing the Python value
##
sub convertToPythonValueFromExpr {
  my $type = $_[0];
  my $iv = $_[1];

  # Check the type is supported
  splToPythonConversionCheck($type);

  return "streamsx::topology::pySplValueToPyObject($iv)";
}

# Check if a type includes blobs in its definition.
# Could be just blob, or list<blob> etc.
# blob is not supported for map/set
sub typeHasBlobs {
  my $type = $_[0];

  if (SPL::CodeGen::Type::isBlob($type)) {
      return 1;
  }
  if (SPL::CodeGen::Type::isList($type)) {
      my $element_type = SPL::CodeGen::Type::getElementType($type);
      return typeHasBlobs($element_type);
  }
  if (SPL::CodeGen::Type::isMap($type)) {
      my $value_type = SPL::CodeGen::Type::getValueType($type);
      return typeHasBlobs($value_type);
  }

  return 0;
}

#
# Return a C++ code block converting a input attribute
# from an SPL input tuple to a Python object and
# setting it into pyTuple (as a Python Tuple).
# Assumes a C++ variable pyTuple is defined.
#
sub convertToPythonValueAsTuple {
  my $ituple = $_[0];
  my $i = $_[1];
  my $type = $_[2];
  my $name = $_[3];
  
  # starts a C++ block and sets pyValue
  my $get = _attr2Value($ituple, $type, $name);

  # Note PyTuple_SET_ITEM steals the reference to the value
  my $assign =  "PyTuple_SET_ITEM(pyTuple, $i, value);\n";

  return $get . $assign . "}\n" ;
}

# Determine which style of argument is being
# used. These match the SPL types in
# com.ibm.streamsx.topology/types.spl
#
# SPL TYPE - style - comment
# blob __spl_po - python - pickled Python object
# rstring string - string - SPL rstring
# rstring jsonString - json - JSON as SPL rstring
# xml document - xml - XML document
# blob binary - binary - Binary data
#
# tuple<...> - dict - Any SPL tuple type apart from above
#
# Not all are supported yet.
# 

sub splpy_tuplestyle{

 my ($port) = @_;

 my $attr =  $port->getAttributeAt(0);
 my $attrtype = $attr->getSPLType();
 my $attrname = $attr->getName();
 my $pystyle = 'unk';
 my $numattrs = $port->getNumberOfAttributes();

 if (($numattrs == 1) && SPL::CodeGen::Type::isBlob($attrtype) && ($attrname eq '__spl_po')) {
    $pystyle = 'pickle';
 } elsif (($numattrs == 1) && SPL::CodeGen::Type::isRString($attrtype) && ($attrname eq 'string')) {
    $pystyle = 'string';
 } elsif (($numattrs == 1) && SPL::CodeGen::Type::isRString($attrtype) && ($attrname eq 'jsonString')) {
    $pystyle = 'json';
 } elsif (($numattrs == 1) && SPL::CodeGen::Type::isBlob($attrtype) && ($attrname eq 'binary')) {
    $pystyle = 'binary';
    SPL::CodeGen::errorln("Blob schema is not currently supported for Python."); 
 } elsif (($numattrs == 1) && SPL::CodeGen::Type::isXml($attrtype) && ($attrname eq 'document')) {
    $pystyle = 'xml';
    SPL::CodeGen::errorln("XML schema is not currently supported for Python."); 
 } else {
    $pystyle = 'dict';
 }

 return $pystyle;
}

# Given a style return a string containing
# the C++ code to get the value
# from an input tuple ip, that will
# be converted to Python and passed to the function.
#
# Must setup a C++ variable called 'value' that
# represents the value to be passed into the Python function
#
sub splpy_inputtuple2value{
 my ($pystyle) = @_;
 if ($pystyle eq 'pickle') {
  return 'SPL::blob const & value = ip.get___spl_po();';
 }

 if ($pystyle eq 'string') {
  return 'SPL::rstring const & value = ip.get_string();';
 }
 
 if ($pystyle eq 'json') {
  return 'SPL::rstring const & value = ip.get_jsonString();';
 }

 if ($pystyle eq 'dict') {
  # nothing done here for dict style 
  # instead cgt is used to generate code specific
  # the input schema
 }
}

# Starts a block that converts an SPL attribute
# to the enclosed variable value
sub _attr2Value {
  my $ituple = $_[0];
  my $type = $_[1];
  my $name = $_[2];

  my $get = '{ PyObject * value = ';
  $get = $get . convertAttributeToPythonValue($ituple, $type, $name);
  $get = $get . ";\n";

  # If the attribute has blobs then
  # save the corresponding memory view objects.
  # We just save the attribute's Python representation
  # Typically, it will be a memoryview (from a blob) but
  # if it's something containing blobs the complete collection
  # object is passed to Python for release.
  #
  # Assumes that pyMvs exists set up by py_splTupleCheckForBlobs.cgt
  if (typeHasBlobs($type)) {
      $get = $get . "PYSPL_MEMORY_VIEW(value);\n";
  }
  return $get;
}

#
# Convert attribute of an SPL tuple to Python
# and add to a dictionary object.
#
# ituple - C++ expression of the tuple
# i  - Attribute index
# type - spl type
# name - attribute name
# names - PyObject * pointing to Python tuple containing attribute names.

sub convertAndAddToPythonDictionaryObject {
  my $ituple = $_[0];
  my $i = $_[1];
  my $type = $_[2];
  my $name = $_[3];
  my $names = $_[4];

  # starts a C++ blockand sets value
  my $get = _attr2Value($ituple, $type, $name);

  # PyTuple_GET_ITEM returns a borrowed reference.
  $getkey = 'PyObject * key = PyTuple_GET_ITEM(' . $names . ',' . $i . ");\n";

# Note PyDict_SetItem does not steal the references to the key and value
  my $setdict =  "PyDict_SetItem(pyDict, key, value);\n";
  $setdict =  $setdict . "Py_DECREF(value);\n";

  return $get . $getkey . $setdict . "}\n" ;
}

## Execute pip to install packages in the
## applications output directory under
## /etc/streamsx.topology/python
## They will be added to Python's path.
## Picks up pip from the path.
##
sub spl_pip_packages {

  my $model = $_[0];
  my $packages = $_[1];
  my $reqFile = $model->getContext()->getToolkitDirectory()."/opt/python/streams/requirements.txt";

  my $needPip = @$packages || -r $reqFile;

  if ($needPip) {

    use File::Path qw(make_path);
    my $pkgDir = $model->getContext()->getOutputDirectory()."/etc/streamsx.topology/python";
    make_path($pkgDir);

# Need pip 9
# '--upgrade-strategy', 'only-if-needed');

    my $pip = 'pip';

    my $rcv2 = `$pip --version`;
    SPL::CodeGen::println("pip version:" . $rcv2);

    my @pipCmd = ($pip, 'install', '--disable-pip-version-check', '--user', '--upgrade');


    print("#if 0\n");
    print("/*\n");

    my $pub = $ENV{'PYTHONUSERBASE'};
    $ENV{'PYTHONUSERBASE'} = $pkgDir;

    if (-r $reqFile) {
      SPL::CodeGen::println("Installing Python packages from requirements:");
      push(@pipCmd, ('--requirement', $reqFile));
      SPL::CodeGen::println("Executing pip:" . join(' ', @pipCmd));
      my $rc = system(@pipCmd);
      if ($rc != 0) {
        SPL::CodeGen::errorln("pip failed for requirements" . join(' ', @pipCmd));
      } 
      pop(@pipCmd);
      pop(@pipCmd);
    }

    if (@$packages) {
      SPL::CodeGen::println("Installing Python packages:" . join(' ', @$packages));
      push(@pipCmd, @$packages);
      SPL::CodeGen::println("Executing pip:" . join(' ', @pipCmd));
      my $rc = system(@pipCmd);
      if ($rc != 0) {
        SPL::CodeGen::errorln("pip failed for packages:" . join(' ', @pipCmd));
      } 
    }

    print("*/\n");
    print("#endif\n");

    if ($pub) {
      $ENV{'PYTHONUSERBASE'} = $pub;
    }
  }
}

1;
