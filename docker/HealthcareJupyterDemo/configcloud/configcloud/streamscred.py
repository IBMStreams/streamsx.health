import json
import os
import configcloud.config as config


def update_env(serviceName, credentialDict):
    """
    Update the environment variables and docker environment
    variables file (env_file).
    """
    vs = {
        'streaming-analytics': [
            {
                'name': serviceName,
                'credentials': credentialDict
            }
        ]
    }
    with open('env_file', 'w') as outfile:
        outfile.write("VCAP_SERVICES=")
        vsJsonStr = json.dumps(vs)
        outfile.write(vsJsonStr)
        outfile.write("\n")
        os.environ["VCAP_SERVICES"] = vsJsonStr

        outfile.write("STREAMING_ANALYTICS_SERVICE_NAME=")
        outfile.write(serviceName)
        outfile.write("\n")
        os.environ["STREAMING_ANALYTICS_SERVICE_NAME"] = serviceName
    print("UPDATING environment : ")
    print(" -- STREAMING_ANALYTICS_SERVICE_NAME : %s" % serviceName)
    print(" -- VCAP_SERVICES : json to follow")
    print(json.dumps(vs, indent=2))
    print("\n\n Environment Configured ")
    return vs


# Check to see if it's necessary todo the processing of this file.
#  Are the VCAP_SERVICES and STREAMING_ANALYTICS_SERVICE_NAME set
def validateVCAP():
    svcs = os.getenv('VCAP_SERVICES', False)
    anaSvc = os.getenv('STREAMING_ANALYTICS_SERVICE_NAME', False)
    if not svcs:
        print("Failed to find VCAP_SERVICES")
        return False
    else:
        vcap = json.loads(svcs)
    if not anaSvc:
        print("Failed to find 'STREAMING_ANALYTICS_SERVICE_NAME'")
        return False

    if 'streaming-analytics' not in vcap:
        print("Failed to find 'streaming-analytics' in VCAP")
        return False

    found = False
    creds = vcap['streaming-analytics']
    for cred in creds:
        if (anaSvc == cred['name']):
            print(" STREAMING_ANALYTICS_SERVICE_NAME & VCAP_SERVICES found and configured")
            print(" STREAMING_ANALYTICS_SERVICE_NAME : %s" % anaSvc)
            return True
    print("Failed to locate STREAMING_ANALYTICS_SERVICE_NAME '", anaSvc, "'in VCAP_SERVICES")
    print(json.dumps(vcap, indent=2))



def service_submit(sender):
    """Get/check service name.."""

    svcName = config.uiText.value.strip()
    if len(svcName):
        config.serviceName = svcName
        print("Service Name:", config.serviceName)
    else:
        print("Invalid ServiceName : length == 0")
        print("ServiceName not defined")


global CredentialDict
CredentialDict = None


def validate_cred(str):

    try:
        credDict = json.loads(str + " ")
    except:
        print("Invalid formatted credentional - should be in a json format")
        return (None)

    credSet = set(list(credDict.keys()))
    validSet = {'iam_apikey_description', 'iam_apikey_name', 'iam_role_crn', 'apikey', 'iam_serviceid_crn',
                'v2_rest_url'}
    if len(validSet.difference(credSet)) is 0:
        return credDict
    else:
        print("Credentialaπ missing keys:", validSet.difference(credSet))
        return None


def vcap_submit(sender):

    str = config.uiText.value
    config.credentialDict = validate_cred(str)
    if config.credentialDict is not None:
        if config.serviceName is not None:
            vsDict = update_env(config.serviceName, config.credentialDict)
