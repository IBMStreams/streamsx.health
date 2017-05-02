#****************************************************************************
# Copyright (C) 2017 International Business Machines Corporation
# All Rights Reserved
# ****************************************************************************

# Convenient methods to check for reading types

from streamsx_health.ingest.Observation import getReadingCode

def isECGLeadI(obxDict):
    """Check if the observation is of reading type ECGLeadI
    Args:
        obxDict(dict): Observation in dictionary
    Returns:
        bool: True if the observation is of this reading type, False otherwise.
    """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X100-8'

def isECGLeadII(obxDict):
    """Check if the observation is of reading type ECGLeadII
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X101-6'

def isECGLeadIII(obxDict):
    """Check if the observation is of reading type ECGLeadIII
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X102-4'

def isECGLeadV1(obxDict):
    """Check if the observation is of reading type ECGLeadV1
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X103-2'

def isECGLeadV2(obxDict):
    """Check if the observation is of reading type ECGLeadV2
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X104-0'

def isECGLeadV3(obxDict):
    """Check if the observation is of reading type ECGLeadV3
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X105-7'

def isECGLeadV4(obxDict):
    """Check if the observation is of reading type ECGLeadV4
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X106-5'

def isECGLeadV5(obxDict):
    """Check if the observation is of reading type ECGLeadV5
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X107-3'

def isECGLeadV6(obxDict):
    """Check if the observation is of reading type ECGLeadV6
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X108-1'

def isECGLeadV7(obxDict):
    """Check if the observation is of reading type ECGLeadV7
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X109-9'

def isECGLeadV8(obxDict):
    """Check if the observation is of reading type ECGLeadV8
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X110-7'

def isECGLeadV9(obxDict):
    """Check if the observation is of reading type ECGLeadV9
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X111-5'

def isECGLeadAVF(obxDict):
    """Check if the observation is of reading type ECGLeadAVF
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X112-3'

def isECGLeadAVL(obxDict):
    """Check if the observation is of reading type ECGLeadAVL
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X113-1'

def isECGLeadAVR(obxDict):
    """Check if the observation is of reading type ECGLeadAVR
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X114-9'

def isHeartRate(obxDict):
    """Check if the observation is of reading type Heart Rate
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == '8867-4'

def isRespiratoryRate(obxDict):
    """Check if the observation is of reading type Respiratory Rate
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == '9279-1'

def isResp(obxDict):
    """Check if the observation is of reading type Respiratory Rate Waveform
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == '76270-8'

def isTemperature(obxDict):
    """Check if the observation is of reading type Temperature
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == '8310-5'

def isSpo2(obxDict):
    """Check if the observation is of reading type SPO2
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == '2710-2'

def isBPSystolic(obxDict):
    """Check if the observation is of reading type BP Systolic
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == '8480-6'

def isBPDiastolic(obxDict):
    """Check if the observation is of reading type BP Diagstolic
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == '8462-4'

def isPleth(obxDict):
    """Check if the observation is of reading type Pleth
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X200-6'

def isPulse(obxDict):
    """Check if the observation is of reading type Pulse
        Args:
            obxDict(dict): Observation in dictionary
        Returns:
            bool: True if the observation is of this reading type, False otherwise.
        """
    readingCode = getReadingCode(obxDict)
    return readingCode == 'X201-4'