from unittest import TestCase
import os
import environment_check_load
import json

def clearEnv():
    if os.getenv(environment_check_load.srvcName, False):
        del os.environ[environment_check_load.srvcName]
    if os.getenv(environment_check_load.vcapName, False):
        del os.environ[environment_check_load.vcapName]


class TestEnvironment_check_load(TestCase):
    def test_load_env_noexistance(self):
        clearEnv()
        tst = environment_check_load.environment_check_load(env_file="x")
        self.assertFalse(tst, "Loading non existent file")

    def test_load__env_default(self):
        clearEnv()
        tst = environment_check_load.environment_check_load("../../notebooks/env_file")
        self.assertFalse(tst, "Fail to load default file")

    def test_load__env_valid(self):
        clearEnv()
        tst = environment_check_load.environment_check_load("../../notebooks/env_file.active")
        self.assertTrue(tst, "Load valid file")
        self.assertTrue(os.getenv(environment_check_load.srvcName, False))
        self.assertTrue(os.getenv(environment_check_load.vcapName, False))

    def test_env_json(self):
        clearEnv()
        tst = environment_check_load.environment_check_load("../../notebooks/env_file.active")
        jsn = os.getenv(environment_check_load.vcapName, False)
        srvc = os.getenv(environment_check_load.srvcName, False)
        dict = json.loads(jsn)
        self.assertTrue("streaming-analytics" in dict, "streaming-analytics no found")
        self.assertTrue(len(dict["streaming-analytics"])==1  , "streaming-analytics not 1")
        self.assertTrue("name" in dict["streaming-analytics"][0] , "name not found in streaming")
        self.assertTrue(srvc == dict["streaming-analytics"][0]["name"] , "failed to locate %s in json" % srvc)






