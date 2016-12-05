#import json
#import numpy as np
import sys,os,os.path
sys.path.append('../ext/biosppy_streaming')

import warnings
from biosppy.signals import ecg_streaming
from decimal import Decimal
from collections import deque
from itertools import filterfalse
import utils

#class RPeakDetect:
#    def __init__(self, maxLen=300):
#        self.ch1Window = deque(maxlen=maxLen)
#        self.tsWindow = deque(maxlen=maxLen)
#
#    def __call__(self, tuple):
#        # print(tuple)
#        self.ch1Window.append(float(tuple['ch1']))
#        self.tsWindow.append(float(tuple['ts']))
#        # print(ch1Window)
#        if len(self.ch1Window) >= 250:
#            out = ecg.christov_segmenter(signal=list(self.ch1Window), sampling_rate=250.0)
#            times = []
#            for i in out['rpeaks']:
#                times.append(self.tsWindow[i])
#            return times

"""
Expects a list of signal values as an input
"""

def streaming_rpeak(signalStream, sampling_rate, data_label):
    return signalStream.transform(_SignalStreamFunctor(sampling_rate=sampling_rate, data_label=data_label)) \
                       .transform(ecg_streaming.preprocess_MA_powerline) \
                       .transform(ecg_streaming.preprocess_MA_filtfilt) \
                       .transform(ecg_streaming.preprocess_MA_lowpass) \
                       .transform(ecg_streaming.preprocess_MA_highpass) \
                       .transform(ecg_streaming.preprocess_setYArray) \
                       .transform(ecg_streaming.complex_lead_lfilter) \
                       .transform(ecg_streaming.complex_lead_algo) \
                       .transform(DataPostProcessing(data_label))

class DataPostProcessing:
    def __init__(self, data_label):
        self.lastTime = -1.
        self.data_label = data_label
        self.rpeak_queue = deque(maxlen=3)

    def __call__(self, tup):
        coords = []
        for rpeak_idx in tup['rpeaks']:
            rpeak_ts = utils.get_sampled_data_values(tup, 'timestamp')[rpeak_idx]
            self.rpeak_queue.append(rpeak_ts)

            if len(self.rpeak_queue) == 3:
                rr = self.rpeak_queue[1] - self.rpeak_queue[0]
                rr_plus1 = self.rpeak_queue[2] - self.rpeak_queue[1]
                coords.append([rr, rr_plus1])

        utils.add_coordinate_data(tup, 'Poincare - ' + self.data_label, coords)

        ## remove keys that are no longer needed
        tup.pop('signal', None)
        tup.pop('__algo_vars', None)
        tup.pop('rpeaks', None)
        tup.pop('sampling_rate', None)

        ## remove data points with a time stamp less than 
        ## the last time stamp recorded
#        dedup_list = []
#        for point in tup['data']:
#            if point['ts'] > self.lastTime:
#                dedup_list.append(point)
#                self.lastTime = point['ts']
#        tup['data'] = dedup_list

        return tup

    def _get_index_from_label(self, tup, label):
        for idx in range(len(tup['data'])):
            if tup['data'][idx]['label'] == label:
                return idx



def _streaming_rpeak_data_updater(tup):
    rpeaks = tup['rpeaks']
    for peak_idx in rpeaks:
        tup['data'][peak_idx]['isRPeak'] = True

    ## remove keys that are no longer needed
    tup.pop('signal', None)
    tup.pop('__algo_vars', None)
    tup.pop('rpeaks', None)

    return tup



class _SignalStreamFunctor:
    def __init__(self, sampling_rate, data_label):
        self.sampling_rate = sampling_rate
        self.data_label = data_label

    def __call__(self, tup):
        signal = []
        for d in tup['data']:
            if d['label'] == self.data_label:
                signal = d['valueSampledData']['values']
                break

        tup['signal'] = signal
        tup['sampling_rate'] = self.sampling_rate

        return tup

"""
Returns a dictionary object with the following keys:
    'data', 'gain' freq

Where the 'data' key holds an array of dictionary objects,
with each object having the following keys:
    'ts', 'value', isRPeak'


"""
class RPeakAggregator:
    def __init__(self, windowLength=100):
        self.window = []
        self.windowLength = windowLength
        self.lastTS = -1

    def __call__(self, data):
        self.window = self.window + data
        
        outTuple = {'patientID' : data[0]['patientID'], 'data' : [], 'gain' : data[0]['gain'], 'freq' : data[0]['freq']}
        if len(self.window) > self.windowLength:
            values = []
            for elem in self.window:
                if elem['ts'] == self.lastTS:
                    continue
                data_elem = {}
                data_elem['ts'] = elem['ts']
                data_elem['value'] = elem['value']
                outTuple['data'].append(data_elem)
        outTuple['data_length'] = len(outTuple['data'])

        if len(outTuple['data']) == 0:
            return None
        else:
            self.window.clear()
            return outTuple

"""
Expects as an input a list, where each element is a 
dictionary object with the keys 'ts' and 'values'

Returns a dictionary object with the following keys:
 'ts', 'value', 'gain', 'freq', 'isRPeak'
"""
class RPeakDetect:
    def __init__(self, sampleRate=100):
        self.window = []
        self.sampleRate = sampleRate
        self.lastTS = -1

    def __call__(self, data):
        self.window = self.window + data

        if len(self.window) > self.sampleRate:
            values = []
            for elem in self.window:
                if elem['ts'] == self.lastTS:
                    continue
                values.append(elem['value'])
                self.lastTS = elem['ts'] ## prevent duplicate timestamps
            rpeakIndices = ecg.christov_segmenter(signal=values, sampling_rate=self.sampleRate)['rpeaks']

            outData = []
            for idx in range(len(self.window)):
                isRPeak = False
                if idx in rpeakIndices:
                    isRPeak = True

                v = self.window[idx]
                v['isRPeak'] = isRPeak
                outData.append(v)
                print(v)

            self.window.clear()
            return outData
                    




