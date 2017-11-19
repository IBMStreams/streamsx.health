# This is a generated module.  Any modifications will be lost.
package TopologySplpyResource;
use strict;
use Cwd qw(abs_path);
use File::Basename;
unshift(@INC, $ENV{STREAMS_INSTALL} . "/system/impl/bin") if ($ENV{STREAMS_INSTALL});
require SPL::Helper;
my $toolkitRoot = dirname(abs_path(__FILE__)) . '/../../..';

sub TOPOLOGY_PYTHONHOME($)
{
   my $defaultText = <<'::STOP::';
PYTHONHOME={0}.
::STOP::
    return SPL::Helper::SPLFormattedMessage($toolkitRoot, "com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0301I", \$defaultText, @_);
}


sub TOPOLOGY_PYTHONHOME_NO($)
{
   my $defaultText = <<'::STOP::';
PYTHONHOME environment variable not set. Please set PYTHONHOME to a valid Python {0} install.
::STOP::
    return SPL::Helper::SPLFormattedMessage($toolkitRoot, "com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0302I", \$defaultText, @_);
}


sub TOPOLOGY_LOAD_LIB($)
{
   my $defaultText = <<'::STOP::';
Loading Python library: {0}.
::STOP::
    return SPL::Helper::SPLFormattedMessage($toolkitRoot, "com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0303I", \$defaultText, @_);
}


sub TOPOLOGY_LOAD_LIB_ERROR($$$)
{
   my $defaultText = <<'::STOP::';
Fatal error: could not open Python library: {0} : {2}. Please set PYTHONHOME to a valid Python {1} install.
::STOP::
    return SPL::Helper::SPLFormattedMessage($toolkitRoot, "com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0304E", \$defaultText, @_);
}


sub TOPOLOGY_IMPORT_MODULE_ERROR($)
{
   my $defaultText = <<'::STOP::';
Fatal error: missing module: {0}.
::STOP::
    return SPL::Helper::SPLFormattedMessage($toolkitRoot, "com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0305E", \$defaultText, @_);
}


sub TOPOLOGY_IMPORT_MODULE($)
{
   my $defaultText = <<'::STOP::';
Imported  module: {0}.
::STOP::
    return SPL::Helper::SPLFormattedMessage($toolkitRoot, "com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0306I", \$defaultText, @_);
}

1;
