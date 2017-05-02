import unittest
import json
from streamsx_health.ingest.Observation import *
from streamsx_health.ingest.ObservationTypeResolver import *

class TestObservationTypeResolver(unittest.TestCase):

    def getObservation(self, code):
        jsonStr = '{"patientId":"patient-1", "device":{"id":"VitalsGenerator", "locationId":"bed1"}, "readingSource": {"id":123, "deviceId":"VitalsGenerator", "sourceType":"generated"}, "reading": {"ts": 605, "uom":"bpm", "value":82.56785326532197, "readingType":'
        jsonStr += '{"code":"' + code + '", "system":"streamsx.heath/1.0"}}}'
        dictObj = json.loads(jsonStr)
        return dictObj

    def __test(self, code, method):
        obx = self.getObservation(code)
        self.assertTrue(method(obx))
        obx = self.getObservation('XXXXXX')
        self.assertFalse(method(obx))

    def test_isHeartRate(self):
        self.__test('8867-4', isHeartRate)


    def test_isRespRate(self):
       self.__test('9279-1', isRespiratoryRate)

    def test_isResp(self):
        self.__test('76270-8', isResp)

    def test_isTemperature(self):
        self.__test('8310-5', isTemperature)

    def test_isSpo2(self):
        self.__test('2710-2', isSpo2)

    def test_isBpSys(self):
        self.__test('8480-6', isBPSystolic)

    def test_isBpDia(self):
        self.__test('8462-4', isBPDiastolic)

    def test_isPleth(self):
        self.__test('X200-6', isPleth)

    def test_isEcgLeadI(self):
        self.__test('X100-8', isECGLeadI)

    def test_isEcgLeadII(self):
        self.__test('X101-6', isECGLeadII)

    def test_isEcgLeadIII(self):
        self.__test('X102-4', isECGLeadIII)

    def test_isEcgLeadV1(self):
        self.__test('X103-2', isECGLeadV1)

    def test_isEcgLeadV2(self):
        self.__test('X104-0', isECGLeadV2)

    def test_isEcgLeadV3(self):
        self.__test('X105-7', isECGLeadV3)

    def test_isEcgLeadV4(self):
        self.__test('X106-5', isECGLeadV4)

    def test_isEcgLeadV5(self):
        self.__test('X107-3', isECGLeadV5)

    def test_isEcgLeadV6(self):
        self.__test('X108-1', isECGLeadV6)

    def test_isEcgLeadV7(self):
        self.__test('X109-9', isECGLeadV7)

    def test_isEcgLeadV8(self):
        self.__test('X110-7', isECGLeadV8)

    def test_isEcgLeadV9(self):
        self.__test('X111-5', isECGLeadV9)

    def test_isEcgLeadAVF(self):
        self.__test('X112-3', isECGLeadAVF)

    def test_isEcgLeadAVL(self):
        self.__test('X113-1', isECGLeadAVL)

    def test_isEcgLeadAVR(self):
        self.__test('X114-9', isECGLeadAVR)

if __name__ == '__main__':
    unittest.main()
