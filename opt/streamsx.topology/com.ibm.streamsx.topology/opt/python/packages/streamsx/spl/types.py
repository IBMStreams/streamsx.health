# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016,2017
"""
SPL type definitions.

SPL is strictly typed, thus when invoking SPL operators
using classes from ``streamsx.spl.op`` then any parameters
must use the SPL type required by the operator.

"""

import datetime

class Timestamp(object):
    """
    SPL naive timestamp type with nanosecond resolution.

    Common usage is to store the seconds and nanoseconds since the Unix Epoch (Jan 1, 1970),
    but this is not enforced by the `Timestamp` class.

    Machine identifier is an application defined identifier for the machine the timestamp
    was created on. It is the responsibility of the application to set the machine identifier
    if required. The machine identifier may be used to detect if two timestamps were created on the same machine,
    as there may be variations in the clocks on different machines.

    Attributes:
        seconds (int) : Seconds since epoch.
        nanoseconds (int) : Nanosecond component.
        machine_id (int) : Machine identifier.
    """

    _EPOCH = datetime.datetime.utcfromtimestamp(0)

    @staticmethod
    def from_datetime(dt, machine_id=0):
        """
        Convert a datetime to an SPL `Timestamp`.
        Args:
           dt(datetime.datetime): Datetime to be converted.
           machine_id(int): Machine identifier.

        Returns:
             Timestamp: SPL timestamp value.
        """
        td = dt - Timestamp._EPOCH
        seconds = td.days * 3600 * 24
        seconds += td.seconds
        return Timestamp(seconds, td.microseconds*1000, machine_id)

    def __init__(self, seconds, nanoseconds, machine_id=0):
        self._seconds = int(seconds)
        self._nanoseconds = int(nanoseconds)
        self._machine_id = int(machine_id)

    @property
    def seconds(self):
        """
        Seconds value.

        Returns:
            int: Seconds value.

        """
        return self._seconds

    @property
    def nanoseconds(self):
        return self._nanoseconds

    @property
    def machine_id(self):
        return self._machine_id

    def time(self):
        """
        Get the time in seconds since the epoch.

        Returns:
            float: time in seconds since the epoch.
        """
        return self.seconds + (self.nanoseconds / 1000000000.0)

    def datetime(self):
        """
        Return the UTC datetime corresponding to the POSIX timestamp.

        This is identical to ```datetime.datetime.utcfromtimestamp(self.time())```.
        Nanosecond resolution may be lost.

        Returns:
             (datetime.datetime):
        """
        return datetime.datetime.utcfromtimestamp(self.time())

    def tuple(self):
        """Return this timestamp as a tuple.

        Returns:
            tuple: Returns a tuple of ``(seconds, nanoseconds, machine_id)``

        """
        return self._seconds, self._nanoseconds, self._machine_id

    def __str__(self):
        """ String representation matching SPL's representation.
        """
        return str(self.tuple())

def _get_timestamp_tuple(ts):
    """
    Internal method to get a timestamp tuple from a value.
    Handles input being a datetime or a Timestamp.
    """
    if isinstance(ts, datetime.datetime):    
        return Timestamp.from_datetime().tuple()
    return ts.tuple()
    
from streamsx.spl.op import Expression

def int8(value):
    """
    Create an SPL ``int8`` value.
    """
    return Expression('INT8', int(value))

def int16(value):
    """
    Create an SPL ``int16`` value.
    """
    return Expression('INT16', int(value))

def int32(value):
    """
    Create an SPL ``int32`` value.
    """
    return Expression('INT32', int(value))

def int64(value):
    """
    Create an SPL ``int64`` value.
    """
    return Expression('INT64', int(value))

def uint8(value):
    """
    Create an SPL ``uint8`` value.
    """
    return Expression('UINT8', int(value))

def uint16(value):
    """
    Create an SPL ``uint16`` value.
    """
    return Expression('UINT16', int(value))

def uint32(value):
    """
    Create an SPL ``uint32`` value.
    """
    return Expression('UINT32', int(value))

def uint64(value):
    """
    Create an SPL ``uint64`` value.
    """
    return Expression('UINT64', int(value))

def float32(value):
    """
    Create an SPL ``float32`` value.
    """
    return Expression('FLOAT32', float(value))

def float64(value):
    """
    Create an SPL ``float64`` value.
    """
    return Expression('FLOAT64', float(value))

def rstring(value):
    """
    Create an SPL ``rstring`` value.
    """
    return Expression('RSTRING', str(value))
