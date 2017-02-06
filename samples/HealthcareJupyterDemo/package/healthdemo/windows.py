# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016, 2017
from collections import deque

class TumblingWindow:
    def __init__ (self, length):
        self.length = length
        self.window = []
    def __call__ (self, tuple):
        self.window.append(tuple)
        if(len(self.window) == self.length):
             temp = self.window
             self.window = []
             return temp

class SlidingWindow:
    def __init__(self, length, trigger):
        self.length = length
        self.trigger = trigger
        self.triggerCount = 0
        self.window = deque(maxlen=length)

    def __call__(self, data):
        self.window.append(data)
        self.triggerCount += 1

        if self.triggerCount == self.trigger:
            self.triggerCount = 0
            return list(self.window)
