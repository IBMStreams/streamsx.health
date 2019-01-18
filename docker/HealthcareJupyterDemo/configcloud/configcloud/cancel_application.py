from streamsx import rest

def cancel_applications(apps=[]):
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
