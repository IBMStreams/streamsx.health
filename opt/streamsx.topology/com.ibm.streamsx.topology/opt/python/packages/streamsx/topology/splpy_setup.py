# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016
import sys
import os

# This file is in opt/python/packages/streamsx/topology
# in the com.ibm.streamsx.topology toolkit
dir = os.path.dirname(__file__)
optpkgs = os.path.dirname(os.path.dirname(dir))

if optpkgs not in sys.path:
    sys.path.insert(0, optpkgs)

