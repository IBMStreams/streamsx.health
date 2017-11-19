# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016,2017
import os.path
import sys
import site
import inspect
import types
import collections
import logging

from streamsx.topology import _debug
import streamsx.topology._stdlib as _stdlib

class _DependencyResolver(object):
    """
    Finds dependencies given a module object
    """
    
    def __init__(self, topology):
        self._modules = set()
        self._packages = collections.OrderedDict() # need an ordered set when merging namespace directories
        self._processed_modules = set()
        # Determine path of opt/python/packages/streamsx
        my_module = sys.modules[self.__module__]
        dir = os.path.dirname(os.path.abspath(my_module.__file__))
        dir = os.path.dirname(dir)
        self._streamsx_topology_dir = dir
        self.topology = topology

    def _find_dependent_modules(self, module):
        """
        Return the set of dependent modules for used modules,
        classes and routines.
        """
        dms = set()
        for um in inspect.getmembers(module, inspect.ismodule):
            dms.add(um[1])

        for uc in inspect.getmembers(module, inspect.isclass):
            self._add_obj_module(dms, uc[1])
        for ur in inspect.getmembers(module, inspect.isroutine):
            self._add_obj_module(dms, ur[1])

        return dms

    def _add_obj_module(self, dms, obj):
        if hasattr(obj, '__module__'):
            if obj.__module__ in sys.modules:
                dms.add(sys.modules[obj.__module__])

    def add_dependencies(self, module):
        """
        Adds a module and its dependencies to the list of dependencies.

        Top-level entry point for adding a module and its dependecies.
        """

        if module in self._processed_modules:
            return None

        if hasattr(module, "__name__"):
            mn = module.__name__
        else:
            mn = '<unknown>'

        _debug.debug("add_dependencies:module=%s", module)

        # If the module in which the class/function is defined is __main__, don't add it. Just add its dependencies.
        if mn == "__main__":
            self._processed_modules.add(module)

        # add the module as a dependency
        elif not self._add_dependency(module, mn):
            _debug.debug("add_dependencies:not added:module=%s", mn)
            return None

        _debug.debug("add_dependencies:ADDED:module=%s", mn)

        # recursively get the module's imports and add those as dependencies
        for dm in self._find_dependent_modules(module):
            _debug.debug("add_dependencies:adding dependent module %s for %s", dm, mn)
            self.add_dependencies(dm)
    
    @property
    def modules(self):
        """
        Property to get the list of module dependencies
        """
        return frozenset(self._modules)
    
    @property
    def packages(self):
        """
        Property to get the list of package dependencies
        """
        return tuple(self._packages.keys())   

    def _include_module(self, module, mn):
        """ See if a module should be included or excluded based upon
        included_packages and excluded_packages.

        As some packages have the following format:

        scipy.special.specfun
        scipy.linalg

        Where the top-level package name is just a prefix to a longer package name,
        we don't want to do a direct comparison. Instead, we want to exclude packages
        which are either exactly "<package_name>", or start with "<package_name>".
        """

        if mn in self.topology.include_packages:
            _debug.debug("_include_module:explicit using __include_packages: module=%s", mn)
            return True
        if '.' in mn:
            for include_package in self.topology.include_packages:
                if mn.startswith(include_package + '.'):
                    _debug.debug("_include_module:explicit pattern using __include_packages: module=%s pattern=%s", mn, \
                            include_package + '.')
                    return True

        if mn in self.topology.exclude_packages:
            _debug.debug("_include_module:explicit using __exclude_packages: module=%s", mn)
            return False
        if '.' in mn:
            for exclude_package in self.topology.exclude_packages:
                if mn.startswith(exclude_package + '.'):
                    _debug.debug("_include_module:explicit pattern using __exclude_packages: module=%s pattern=%s", mn, \
                                 exclude_package + '.')
                    return False

        _debug.debug("_include_module:including: module=%s", mn)
        return True

    def _add_dependency(self, module, mn):
        """
        Adds a module to the list of dependencies
        wihtout handling the modules dependences.
        """
        _debug.debug("_add_dependency:module=%s", mn)

        if _is_streamsx_module(module):
            _debug.debug("_add_dependency:streamsx module=%s", mn)
            return False

        if _is_builtin_module(module):
            _debug.debug("_add_dependency:builtin module=%s", mn)
            return False

        if not self._include_module(module, mn):
          #print ("ignoring dependencies for {0} {1}".format(module.__name__, module))
          return False

        package_name = _get_package_name(module)
        top_package_name = module.__name__.split('.')[0]

        if package_name and top_package_name in sys.modules:
            # module is part of a package
            # get the top-level package
            top_package = sys.modules[top_package_name]

            if "__path__" in top_package.__dict__:
                # for regular packages, there is one top-level directory
                # for namespace packages, there can be more than one.
                # they will be merged in the bundle
                for top_package_path in reversed(list(top_package.__path__)):
                    top_package_path = os.path.abspath(top_package_path)
                    self._add_package(top_package_path)
            elif hasattr(top_package, '__file__'):
                # package that is an individual python file with empty __path__
                #print ("Adding package that is an individual file", top_package)
                self._add_package(os.path.abspath(top_package.__file__))
        elif hasattr(module, '__file__'):
            # individual Python module
            module_path = os.path.abspath(module.__file__)
            self._modules.add(module_path)
            
        self._processed_modules.add(module)
        return True

    def _add_package(self, path):
        if path == self._streamsx_topology_dir:
            return None
        self._packages[path] = None
    
#####################
# Utility functions #
#####################
    
def _get_package_name(module):
    """
    Gets the package name given a module object
    
    Returns:
        str: If the module belongs to a package, the package name.  
             if the module does not belong to a package, None or ''.
    """
    try:
        # if __package__ is defined, use it
        package_name = module.__package__
    except AttributeError:
        package_name = None  
        
    if package_name is None:
        # if __path__ is defined, the package name is the module name
        package_name = module.__name__
        if not hasattr(module, '__path__'):
            # if __path__ is not defined, the package name is the
            # string before the last "." of the fully-qualified module name
            package_name = package_name.rpartition('.')[0]
           
    return package_name
    
def _get_module_name(function):
    """
    Gets the function's module name
    Resolves the __main__ module to an actual module name
    Returns:
        str: the function's module name
    """
    module_name = function.__module__
    if module_name == '__main__':
        # get the main module object of the function
        main_module = inspect.getmodule(function)
        # get the module name from __file__ by getting the base name and removing the .py extension
        # e.g. test1.py => test1
        if hasattr(main_module, '__file__'):
            module_name = os.path.splitext(os.path.basename(main_module.__file__))[0]
    return module_name

def _is_builtin_module(module):
    """Is builtin or part of standard library
    """
    if (not hasattr(module, '__file__')) or  module.__name__ in sys.builtin_module_names:
        return True
    if module.__name__ in _stdlib._STD_LIB_MODULES:
        return True
    if not '.' in module.__name__:
        return False
    mn_top = module.__name__.split('.')[0]
    return mn_top in _stdlib._STD_LIB_MODULES
    
        


def _is_streamsx_module(module):
    if hasattr(module, '__name__'):
        mn = module.__name__
        if not mn.startswith('streamsx.'):
            return False
        if mn.startswith('streamsx.topology'):
            return True
        if mn.startswith('streamsx.spl'):
            return True
        if mn.startswith('streamsx.rest'):
            return True
        if mn == 'streamsx.ec':
            return True
        if mn == 'streamsx.st':
            return True
    return False
