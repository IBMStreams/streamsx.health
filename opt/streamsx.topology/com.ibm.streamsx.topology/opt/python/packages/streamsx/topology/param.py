# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016

class OpParam(object) :
    """generic operator parameter"""

    def __init__(self, type, value):
        self._type=type
        self._value=value

    def spl_json(self):
        _splj = {}
        _splj["type"] = self._type 
        _splj["value"] = self._value
        return _splj


def toolkit_dir() :
    return OpParam("splexpr", "getThisToolkitDir()")
