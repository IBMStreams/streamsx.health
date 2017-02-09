# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016, 2017
import subprocess
import queue

class Error(Exception):
    pass

class Streamtool:
    def __init__(self):
        self.job_ids = []

    def submitjob(self, sab_path, params=[]):
        submit_list = ['streamtool', 'submitjob', sab_path]
        submit_list.extend(params)
        p = subprocess.Popen(submit_list,
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)
        for line in p.stdout:
            print(line, end='')
            if 'CDISC0020I Submitted job IDs:' in line:
                s = line.partition(':')
                if s[2] is not '':
                    self.job_ids.append(s[2].strip())

    def canceljob(self, job_id=None):
        if job_id is None:
            if len(self.job_ids) == 0:
                raise Error("No jobs in queue to cancel")
            job_id = self.job_ids.pop()

        p = subprocess.Popen(['streamtool', 'canceljob', str(job_id)],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)
        for line in p.stdout:
            print(line, end='')

    def geturl(self=None):
        p = subprocess.Popen(['streamtool', 'geturl'],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)
        for line in p.stdout:
            print(line, end='');
