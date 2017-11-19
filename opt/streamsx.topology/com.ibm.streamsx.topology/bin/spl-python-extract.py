# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016,2017
import os
import sys
import inspect

# Ensure we have the matching streamsx packages in our path.
_this_dir = os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))
_tk_dir = os.path.dirname(_this_dir)
_py_dir = os.path.join(_tk_dir, 'opt', 'python', 'packages')  
sys.path.insert(0, _py_dir)

import streamsx.scripts.extract as sse

sse._extract_from_toolkit()
sys.exit(0)
