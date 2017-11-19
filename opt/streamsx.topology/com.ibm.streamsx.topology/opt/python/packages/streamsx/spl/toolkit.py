# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2017

import os
from streamsx.topology.topology import Topology

def add_toolkit(topology, location):
    """Add an SPL toolkit to a topology.

    Args:
        topology(Topology): Topology to include toolkit in.
        location(str): Location of the toolkit directory.
    """
    assert isinstance(topology, Topology)
    tkinfo = dict()
    tkinfo['root'] = os.path.abspath(location)
    topology.graph._spl_toolkits.append(tkinfo)

