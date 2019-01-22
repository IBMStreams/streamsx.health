from unittest import TestCase
import configcloud.environment_check_load as ecl
import os
import configcloud.config as config


import json

def clearEnv():
    if os.getenv(config.srvcName, False):
        del os.environ[config.srvcName]
    if os.getenv(config.vcapName, False):
        del os.environ[config.vcapName]


class TestEnvironment_check_load(TestCase):
    def test_load_env_noexistance(self):
        clearEnv()
        tst = ecl.environment_check_load(env_file="xxxx")
        self.assertFalse(tst, "Loading non existent file, environment values not set")

    def test_load__env_default(self):
        clearEnv()
        tst = ecl.environment_check_load("../../notebooks/env_file")
        self.assertFalse(tst, "Default file exists, does not have evironment values set.")

    def test_load__env_valid(self):
        clearEnv()
        tst = ecl.environment_check_load("../../notebooks/env_file.active")
        self.assertTrue(tst, "Load valid file")
        self.assertTrue(os.getenv(config.srvcName, False))
        self.assertTrue(os.getenv(config.vcapName, False))

    def test_env_json(self):
        clearEnv()
        tst = ecl.environment_check_load("../../notebooks/env_file.active")
        jsn = os.getenv(config.vcapName, False)
        srvc = os.getenv(config.srvcName, False)
        dict = json.loads(jsn)
        self.assertTrue("streaming-analytics" in dict, "streaming-analytics no found")
        self.assertTrue(len(dict["streaming-analytics"])==1  , "streaming-analytics not 1")
        self.assertTrue("name" in dict["streaming-analytics"][0] , "name not found in streaming")
        self.assertTrue(srvc == dict["streaming-analytics"][0]["name"] , "failed to locate %s in json" % srvc)






