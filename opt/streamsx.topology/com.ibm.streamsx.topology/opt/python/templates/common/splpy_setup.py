# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015

import os
import sys
import inspect
import time

# Add
# 
# opt/.__splpy/common/packages
# opt/python/packages
# opt/python/modules
# opt/python/streams
#
# for this toolkitto the current Python path.

# This is executed at runtime by the initialization
# of a Python operator

# This file is contained in
# toolkit_root/opt/.__splpy/common

def __splpy_addDirToPath(dir):
    if os.path.isdir(dir):
        if dir not in sys.path:
            sys.path.insert(0, dir)
        
commonDir = os.path.dirname(os.path.realpath(__file__))
splpyDir = os.path.dirname(commonDir)
optDir = os.path.dirname(splpyDir)
pythonDir = os.path.join(optDir, 'python')

__splpy_addDirToPath(os.path.join(splpyDir, 'packages'))

__splpy_addDirToPath(os.path.join(pythonDir, 'streams'))
__splpy_addDirToPath(os.path.join(pythonDir, 'packages'))
__splpy_addDirToPath(os.path.join(pythonDir, 'modules'))
