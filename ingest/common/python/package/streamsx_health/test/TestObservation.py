import unittest
import json
from streamsx_health.ingest.Observation import *

class TestObservation(unittest.TestCase):

    def getObservation(self):
        jsonStr = '{"patientId":"patient-1", "device":{"id":"VitalsGenerator", "locationId":"bed1"}, "readingSource": {"id":123, "deviceId":"VitalsGenerator", "sourceType":"generated"}, "reading": {"ts": 605, "uom":"bpm", "value":82.56785326532197, "readingType": {"code":"8867-4", "system":"streamsx.heath/1.0"}}}'
        dictObj = json.loads(jsonStr)
        return dictObj


    def test_getPatientReadingCode(self):
        obx = self.getObservation()
        self.assertEqual(getReadingCode(obx), '8867-4')


    def test_geReadingCodeSystem(self):
        obx = self.getObservation()
        self.assertEqual(getReadingCodeSystem(obx), 'streamsx.heath/1.0')

    def test_geReadingValue(self):
            obx = self.getObservation()
            self.assertEqual(getReadingValue(obx), 82.56785326532197)

    def test_getUom(self):
        obx = self.getObservation()
        self.assertEqual(getUom(obx), 'bpm')

    def test_getReadingTs(self):
        obx = self.getObservation()
        self.assertEqual(getReadingTs(obx), 605)

    def test_getReadingSourceId(self):
        obx = self.getObservation()
        self.assertEqual(getReadingSourceId(obx), '123')

    def test_getReadingSourceType(self):
        obx = self.getObservation()
        self.assertEqual(getReadingSourceType(obx), 'generated')

    def test_getDeviceId(self):
        obx = self.getObservation()
        self.assertEqual(getDeviceId(obx), 'VitalsGenerator')

    def test_getLocationId(self):
        obx = self.getObservation()
        self.assertEqual(getLocationId(obx), 'bed1')

    def test_getPatientId(self):
        obx = self.getObservation()
        self.assertEqual(getPatientId(obx), 'patient-1')


if __name__ == '__main__':
    unittest.main()
