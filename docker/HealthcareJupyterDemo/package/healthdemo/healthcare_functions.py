# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016, 2017
from scipy import signal
from .utils import get_patient_id

"""
Expects as input a dictionary object

Returns a list of dictionary objects
"""
class RPeakReSample:
    def __init__(self, targetSampleRate):
        self.targetSampleRate = targetSampleRate
        self.timeDelta = -1
        self.lastTime = -1

    def __call__(self, data):
        patientID = data[0]['patientID']
        gain = data[0]['gain']
        freq = self.targetSampleRate
        values = []
        times = []
        for d in data:
            times.append(d['ts'])
            values.append(d['value'])

        ## resample signal
        rs = signal.resample(values, self.targetSampleRate).tolist()

        ## calculate new timestamp
        if self.timeDelta == -1: # calculate delta once
            self.timeDelta = (times[len(times)-1] - times[0])/len(rs)
            self.lastTime = times[0] - self.timeDelta

        resampled_data = {'patientID' : patientID, 'gain' : gain, 'freq' : freq, 'data' : []}
        for i in range(len(rs)):
            self.lastTime += self.timeDelta
            resampled_data['data'].append({'ts' : self.lastTime, 'value' : rs[i], 'isRPeak' : False})
        resampled_data['data_length'] = len(resampled_data['data'])

        return resampled_data

class ReSample:
    def __init__(self, targetSampleRate, sigExcludes=[]):
        self.targetSampleRate = targetSampleRate
        self.sigExcludes = sigExcludes

    def __call__(self, tup):
        for d in tup['data']:
            print("label: " + d['label'])
            if d['label'] not in self.sigExcludes \
            and 'valueSampledData' in d \
            and len(d['valueSampledData']['values']) > 0:
                rs = signal.resample(d['valueSampledData']['values'], self.targetSampleRate).tolist()
                d['valueSampledData']['values'] = rs
                d['valueSampledData']['period']['value'] = (1 / self.targetSampleRate) * 1000
                d['valueSampledData']['period']['unit'] = 'ms'

        return tup

class GenTimestamp:
    def __init__(self, sampling_rate):
        self.ts = {}
        self.sampling_rate = sampling_rate

    def __call__(self, tup):
        patientId = get_patient_id(tup)
        ts = self.ts.setdefault(patientId, 0)
        periodvalue = 1/self.sampling_rate
        d = {'label' : 'timestamp', 'valueSampledData' : {
           'period' : {
               'unit' : 's', 
               'value' : periodvalue
           },
           'values' : [ts]
        }}
        tup['data'].append(d)

        self.ts[patientId] += periodvalue
        return tup

class PatientFilter:
    def __init__(self, patient_id=None):
        self.patient_id = patient_id
    def __call__(self, tup):
        return True if self.patient_id == None else self.patient_id == get_patient_id(tup)

def aggregate(windowedTups):
    if len(windowedTups) == 0:
        return None;

    tup = windowedTups[0]
    for t in windowedTups:
        for idx in range(len(t['data'])):
            tvals = t['data'][idx]['valueSampledData']['values']
            if len(tvals) > 0:
                tup['data'][idx]['valueSampledData']['values'].extend(tvals)

    return tup
