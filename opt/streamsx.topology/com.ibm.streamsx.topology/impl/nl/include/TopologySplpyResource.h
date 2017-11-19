// This is a generated header.  Any modifications will be lost.
#ifndef NL_TOPOLOGYSPLPYRESOURCE_H
#define NL_TOPOLOGYSPLPYRESOURCE_H

#include <SPL/Runtime/Utility/FormattableMessage.h>

#define TOPOLOGY_PYTHONHOME(p0) \
   (::SPL::FormattableMessage1<typeof(p0)>("com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0301I", "PYTHONHOME={0}.", p0))

#define TOPOLOGY_PYTHONHOME_NO(p0) \
   (::SPL::FormattableMessage1<typeof(p0)>("com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0302I", "PYTHONHOME environment variable not set. Please set PYTHONHOME to a valid Python {0} install.", p0))

#define TOPOLOGY_LOAD_LIB(p0) \
   (::SPL::FormattableMessage1<typeof(p0)>("com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0303I", "Loading Python library: {0}.", p0))

#define TOPOLOGY_LOAD_LIB_ERROR(p0, p1, p2) \
   (::SPL::FormattableMessage3<typeof(p0),typeof(p1),typeof(p2)>("com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0304E", "Fatal error: could not open Python library: {0} : {2}. Please set PYTHONHOME to a valid Python {1} install.", p0, p1, p2))

#define TOPOLOGY_IMPORT_MODULE_ERROR(p0) \
   (::SPL::FormattableMessage1<typeof(p0)>("com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0305E", "Fatal error: missing module: {0}.", p0))

#define TOPOLOGY_IMPORT_MODULE(p0) \
   (::SPL::FormattableMessage1<typeof(p0)>("com.ibm.streamsx.topology", "TopologySplpyResource", "en_US/TopologySplpyResource.xlf", "CDIST0306I", "Imported  module: {0}.", p0))

#endif  // NL_TOPOLOGYSPLPYRESOURCE_H
