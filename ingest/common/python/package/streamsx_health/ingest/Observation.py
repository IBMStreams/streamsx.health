#****************************************************************************
# Copyright (C) 2017 International Business Machines Corporation
# All Rights Reserved
# ****************************************************************************

# Get Reading Information
def getReading(obxDict):
    """Get reading part from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        dict: Dictionary contaning a reading
    """
    return obxDict.get('reading')

def getReadingCode(obxDict):
    """Get reading code from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        str: Reading code of the observation.  Value of the code depends on the reading system used.
    """
    reading = getReading(obxDict)
    readingType = reading.get('readingType')
    readingCode = readingType.get('code')
    return str(readingCode)

def getReadingCodeSystem(obxDict):
    """Get reading code system from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        str: Code system used to describe the reading type.  The platform's system code is streamsx.heath/1.0
    """
    reading = getReading(obxDict)
    readingType = reading.get('readingType')
    codeSystem = readingType.get('system')
    return str(codeSystem)

def getReadingValue(obxDict):
    """Get reading value from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        float: Value of the reading
    """
    reading = getReading(obxDict)
    return float(reading.get('value'))

def getReadingValueString(obxDict):
    """Get reading value from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        float: Value of the reading
    """
    reading = getReading(obxDict)
    return str(reading.get('valueString'))

def getUom(obxDict):
    """Get the unit of measure from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        str: The unit of measure fo the observation
    """
    reading = getReading(obxDict)
    return str(reading.get('uom'))

def getReadingTs(obxDict):
    """Get the timestamp from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        int: Timestamp of when the observation is taken
    """
    reading = getReading(obxDict)
    return int(reading.get('ts'))

# Get Reading Source Information

def getReadingSource(obxDict):
    """Get reading source part from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        dict: Dictionary containing information about reading source of the observation
    """
    return obxDict.get('readingSource')

def getReadingSourceId(obxDict):
    """Get reading source id from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        str: Reading source ID - If the device contains multiple channels, reading source Id will represent the channel of which the observation is taken.
    """
    readingSrc = getReadingSource(obxDict)
    return str(readingSrc.get('id'))

def getReadingSourceType(obxDict):
    """Get reading source type from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        str: Type of reading source
    """
    readingSrc = getReadingSource(obxDict)
    return str(readingSrc.get('sourceType'))

# Get Device Information
def getDevice(obxDict):
    """Get device part from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        dict: Device information from the observation
    """
    return obxDict.get('device')

def getDeviceId(obxDict):
    """Get device id from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        str: Device ID from the observation
    """
    return getDevice(obxDict).get('id')

def getLocationId(obxDict):
    """Get locationId of the device from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        str: Location Id of the device
    """
    return str(getDevice(obxDict).get('locationId'))

# Get Patient Id

def getPatientId(obxDict):
    """Get patientId from the observation
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        str: Patient Id
    """
    return str(obxDict.get('patientId'))
