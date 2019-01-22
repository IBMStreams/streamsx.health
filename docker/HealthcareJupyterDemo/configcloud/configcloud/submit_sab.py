import os
from streamsx import rest


def submit_sab(sabFile):
    sc = rest.StreamingAnalyticsConnection()
    instances = sc.get_instances()
    if os.path.exists(sabFile):
        print("Submit", sabFile)
        sub = instances[0].submit_job(sabFile)
    else:
        print("ERR: File does not exist -hmmm", sabFile)

    print("Status of submitted '", sabFile, "':", sub.status)