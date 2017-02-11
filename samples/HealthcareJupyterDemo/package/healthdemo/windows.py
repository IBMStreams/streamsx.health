# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016, 2017
from collections import deque
from .utils import get_patient_id

class TumblingWindow:
    def __init__ (self, length):
        self.length = length
    
    def __call__ (self, tuple):
        patientId = get_patient_id(tuple)
        self.window.setdefault(patientId, [])
        
        self.window[patientId].append(tuple)
        if(len(self.window[patientId]) == self.length):
             temp = self.window[patientId]
             self.window[patientId] = []
             return temp

class SlidingWindow:
    def __init__(self, length, trigger):
        self.length = length
        self.trigger = trigger
        self.triggerCount = {}
        self.window = {}

    def __call__(self, data):
        patientId = get_patient_id(data)
        self.triggerCount.setdefault(patientId, 0)
        self.window.setdefault(patientId, deque(maxlen=self.length))
        
        self.window[patientId].append(data)
        self.triggerCount[patientId] += 1

        if self.triggerCount[patientId] == self.trigger:
            self.triggerCount[patientId] = 0
            return list(self.window[patientId])
