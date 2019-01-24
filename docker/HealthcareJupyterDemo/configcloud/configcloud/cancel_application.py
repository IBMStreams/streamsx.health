from streamsx import rest

def cancel_applications(apps=[]):
    """
    Cancel applications running on th
    :param apps: list of applications to cancel
    :return: True
    """
    sc = rest.StreamingAnalyticsConnection()
    instances = sc.get_instances()

    for instance in sc.get_instances():
        jobs = instance.get_jobs()
        for job in jobs:
            for app in apps:
                # print(job.applicationName,":", job.health)
                if job.applicationName.endswith(app):
                    print("canceling:", job.applicationName)
                    job.cancel()
    print("\nCleanup Done")
    return True
