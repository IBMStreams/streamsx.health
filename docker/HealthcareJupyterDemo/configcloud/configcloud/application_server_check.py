
from streamsx import rest

def application_server_check():
    """
    Check the state of the applications and application server accessed via VCAP. 
    :return: 
    """
    sc = rest.StreamingAnalyticsConnection()
    ana = sc.get_streaming_analytics()
    stat = ana.get_instance_status()
    print("Current state of instance:", stat['state'])

    if stat['state'] == ("STOPPED"):
        print("Starting instance, wait for 'Done' before proceding")
        print(ana.start_instance())
        print("'Done'")
    stat = ana.get_instance_status()
    if (stat['state'] == "STOPPED"):
        print("ERROR * would not start - problems abound.")
        return False
    instances = sc.get_instances()
    for instance in instances:
        print("Status:", instance.status)
    return True

