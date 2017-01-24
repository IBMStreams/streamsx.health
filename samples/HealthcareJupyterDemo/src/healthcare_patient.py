import subprocess
import json

from streamsx.topology.topology import Topology, schema
from streamsx.topology.context import ConfigParams, submit
from streamsx.topology import functions

from patientmonitoring_functions import RPeakDetect, RPeakAggregator
import patientmonitoring_functions
import healthcare_functions
from windows import *

class HealthcarePatientData:
    def __init__(self, vcap, username, password, sample_rate=125, patient_id=None):
        self.vcap = vcap
        self.username = username
        self.password = password
        self.sample_rate = sample_rate
        self.target_sample_rate = 100 
        self.patient_id=patient_id

    def run(self, context='DISTRIBUTED'):
        ## Create topology
        topo = Topology('HealthcareDemo')

        ## Ingest, preprocess and aggregate patient data
        patientData = topo.subscribe('ingest-physionet', schema.CommonSchema.Json) \
                          .map(functions.identity) \
                          .filter(healthcare_functions.PatientFilter(self.patient_id)) \
                          .transform(healthcare_functions.GenTimestamp(self.sample_rate)) \
                          .transform(SlidingWindow(length=self.sample_rate, trigger=self.sample_rate-1)) \
                          .transform(healthcare_functions.aggregate) \

        ## Calculate RPeak and RR delta
        rpeak_data_stream = patientmonitoring_functions.streaming_rpeak(patientData, self.sample_rate, data_label='ECG Lead II')

        ## Create a view of the data
        self.view_data = rpeak_data_stream.view()

        ## Setup VCAP credential for remote build/submit
        cfg = {}
        if self.vcap and self.vcap.strip():
          vcap_json = json.loads(self.vcap)
          service_name = vcap_json['streaming-analytics'][0]['name']
          cfg[ConfigParams.VCAP_SERVICES] = vcap_json
          cfg[ConfigParams.SERVICE_NAME] = service_name  

        ## Compile Python Streams application and submit job
        submit(context, topo.graph, config=cfg, username=self.username, password=self.password)


    '''Access view data'''
    def get_data(self):
        return self.view_data.start_data_fetch()


class PatientIngestService:
    def __init__(self, services_dir="../services", num_patients=1):
        self.services_dir = services_dir
        self.num_patients = num_patients
    def run(self):
        p = subprocess.Popen(["streamtool", "submitjob", self.services_dir + "/com.ibm.streamsx.health.physionet.PhysionetIngestServiceMulti.sab", "-P", "num.patients=" + str(self.num_patients)], 
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)
        for line in p.stdout:
            print(line, end='')

'''
class PatientAnalysisService:
    def __init__(self, services_dir="../services", patient_id):
        self.services_dir = services_dir
        self.patient_id = patient_id
    def run(self):
        p = subprocess.Popen(["streamtool", "submitjob", self.services_dir + "/ECGPatientDataViz.ECGPatientDataViz." + self.patient_id + ".sab"],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)
        for line in p.stdout:
            print(line, end='')
 '''

if __name__ == '__main__':
#    src = PatientIngestService()
#    src.run()
   
    import getpass
    user_ = input('Username: ')
    pass_ = getpass.getpass(prompt='Password: ')

    hc = HealthcarePatientData(username=user_, password=pass_, patient_id='patient-1')
    hc.run(context="BUILD_ARCHIVE")
