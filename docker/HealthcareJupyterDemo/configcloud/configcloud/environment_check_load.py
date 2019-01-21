import configparser
from itertools import chain
import os

vcapName = 'VCAP_SERVICES'
srvcName = 'STREAMING_ANALYTICS_SERVICE_NAME'


def environment_check_load(env_file="env_file"):
    """
    If the configuration notebook has been run and the image not been
    rebooted the environment values will not have been set.
    In that case I read the env_file and populate the environment.
    """
    if os.environ.get(vcapName, None) is not None:
        print("Environment configured.")
        return (True)
    else:
        config = configparser.ConfigParser()
        try:
            with open(env_file) as lines:
                lines = chain(("[top]",), lines)  # This line does the trick - fake section.
                try:
                    print("Loading environment from file.")
                    config.read_file(lines)
                    os.environ[vcapName] = config.get('top', vcapName, fallback="Failed to locate value in env_file ")
                    os.environ[srvcName] = config.get('top', srvcName, fallback="Failed to locate value in env_file.")
                except:
                    print("ERR: Environment contents invalid, has the configration notebook been run? ")
                    return False
                return True
        except FileNotFoundError as err:
            print("ERR: Unable to locate environment file : %s" % env_file)
            return False
    return True

if __name__ == '__main__':
    print("Success : %r " % environment_check_load("../../notebooks/env_file"))

