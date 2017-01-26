# -*- coding: utf-8 -*-
"""
    ecg_streaming
    -------------

    Modified from biosppy.signals.ecg and biosppy.signals.tools

    Generate tuples using ECG R-peak segmentation algorithm.
    Following the approach by Christov

    :copyright: (c) 2016 by IBM Corp.
    :copyright: (c) 2015 by Instituto de Telecomunicacoes
"""

# Imports
# 3rd party
import numpy as np
import scipy.signal as ss

from . import ecg_tools as st

def preprocess_MA_powerline(tup):
    # Pre-processing
    # 1. Moving averaging filter for power-line interference suppression:
    # averages samples in one period of the powerline
    # interference frequency with a first zero at this frequency.
    b = np.ones(int(0.02 * tup['sampling_rate'])) / 50.
    a = [1]
    X = ss.filtfilt(b, a, tup['signal'])
    length = len(tup['signal'])

    tup['__algo_vars'] = {'b' : b, 'a' : a, 'X' : X, 'length' : length}

    return tup

def preprocess_MA_filtfilt(tup):
    # 2. Moving averaging of samples in 28 ms interval for electromyogram
    # noise suppression a filter with first zero at about 35 Hz.
    tup['__algo_vars']['b'] = np.ones(tup['sampling_rate'] / 35.) / 35.
    tup['__algo_vars']['X'] = ss.filtfilt(tup['__algo_vars']['b'], tup['__algo_vars']['a'], tup['__algo_vars']['X'])

    return tup

def preprocess_MA_lowpass(tup):
    tup['__algo_vars']['X'], _, _ = st.filter_signal(signal=tup['__algo_vars']['X'],
                               ftype='butter',
                               band='lowpass',
                               order=7,
                               frequency=40.,
                               sampling_rate=tup['sampling_rate'])
    return tup

def preprocess_MA_highpass(tup):
    tup['__algo_vars']['X'], _, _ = st.filter_signal(signal=tup['__algo_vars']['X'],
                               ftype='butter',
                               band='highpass',
                               order=7,
                               frequency=9.,
                               sampling_rate=tup['sampling_rate'])

    return tup

def preprocess_setYArray(tup):
    k, Y, L = 1, [], len(tup['__algo_vars']['X'])
    for n in range(k + 1, L - k):
        Y.append(tup['__algo_vars']['X'][n] ** 2 - tup['__algo_vars']['X'][n - k] * tup['__algo_vars']['X'][n + k])
    Y = np.array(Y)
    Y[Y < 0] = 0

    tup['__algo_vars']['Y'] = Y
    return tup

def complex_lead_lfilter(tup):
    tup['__algo_vars']['b'] = np.ones(tup['sampling_rate'] / 25.) / 25.
    tup['__algo_vars']['Y'] = ss.lfilter(tup['__algo_vars']['b'], tup['__algo_vars']['a'], tup['__algo_vars']['Y'])

    return tup

def complex_lead_algo(tup):
    # algorithm parameters
    sampling_rate = tup['sampling_rate']
    Y = tup['__algo_vars']['Y']
    v100ms = int(0.1 * sampling_rate)
    v50ms = int(0.050 * sampling_rate)
    v300ms = int(0.300 * sampling_rate)
    v350ms = int(0.350 * sampling_rate)
    v200ms = int(0.2 * sampling_rate)
    v1200ms = int(1.2 * sampling_rate)
    M_th = 0.4  # paper is 0.6

    # Init
    MM = M_th * np.max(Y[:5 * sampling_rate]) * np.ones(5)
    MMidx = 0
    M = np.mean(MM)
    slope = np.linspace(1.0, 0.6, int(sampling_rate))
    Rdec = 0
    R = 0
    RR = np.zeros(5)
    RRidx = 0
    Rm = 0
    QRS = []
    Rpeak = []
    current_sample = 0
    skip = False
    F = np.mean(Y[:v350ms])

    # Go through each sample
    while current_sample < len(Y):
        if QRS:
            # No detection is allowed 200 ms after the current one. In
            # the interval QRS to QRS+200ms a new value of M5 is calculated: newM5 = 0.6*max(Yi)
            if current_sample <= QRS[-1] + v200ms:
                Mnew = M_th * max(Y[QRS[-1]:QRS[-1] + v200ms])
                # The estimated newM5 value can become quite high, if
                # steep slope premature ventricular contraction or artifact
                # appeared, and for that reason it is limited to newM5 = 1.1*M5 if newM5 > 1.5* M5
                # The MM buffer is refreshed excluding the oldest component, and including M5 = newM5.
                Mnew = Mnew if Mnew <= 1.5 * MM[MMidx - 1] else 1.1 * MM[MMidx - 1]
                MM[MMidx] = Mnew
                MMidx = np.mod(MMidx + 1, 5)
                # M is calculated as an average value of MM.
                Mtemp = np.mean(MM)
                M = Mtemp
                skip = True
            # M is decreased in an interval 200 to 1200 ms following
            # the last QRS detection at a low slope, reaching 60 % of its
            # refreshed value at 1200 ms.
            elif current_sample >= QRS[-1] + v200ms and current_sample < QRS[-1] + v1200ms:
                M = Mtemp * slope[current_sample - QRS[-1] - v200ms]
            # After 1200 ms M remains unchanged.
            # R = 0 V in the interval from the last detected QRS to 2/3 of the expected Rm.
            if current_sample >= QRS[-1] and current_sample < QRS[-1] + (2 / 3.) * Rm:
                R = 0
            # In the interval QRS + Rm * 2/3 to QRS + Rm, R decreases
            # 1.4 times slower then the decrease of the previously discussed
            # steep slope threshold (M in the 200 to 1200 ms interval).
            elif current_sample >= QRS[-1] + (2 / 3.) * Rm and current_sample < QRS[-1] + Rm:
                R += Rdec
            # After QRS + Rm the decrease of R is stopped
            # MFR = M + F + R
        MFR = M + F + R
        # QRS or beat complex is detected if Yi = MFR
        if not skip and Y[current_sample] >= MFR:
            QRS += [current_sample]
            Rpeak += [QRS[-1] + np.argmax(Y[QRS[-1]:QRS[-1] + v300ms])]
            if len(QRS) >= 2:
                # A buffer with the 5 last RR intervals is updated at any new QRS detection.
                RR[RRidx] = QRS[-1] - QRS[-2]
                RRidx = np.mod(RRidx + 1, 5)
        skip = False
        # With every signal sample, F is updated adding the maximum
        # of Y in the latest 50 ms of the 350 ms interval and
        # subtracting maxY in the earliest 50 ms of the interval.
        if current_sample >= v350ms:
            Y_latest50 = Y[current_sample - v50ms:current_sample]
            Y_earliest50 = Y[current_sample - v350ms:current_sample - v300ms]
            F += (max(Y_latest50) - max(Y_earliest50)) / 1000.
        # Rm is the mean value of the buffer RR.
        Rm = np.mean(RR)
        current_sample += 1

    rpeaks = []
    for i in Rpeak:
        a, b = i - v100ms, i + v100ms
        if a < 0:
            a = 0
        if b > tup['__algo_vars']['length']:
            b = tup['__algo_vars']['length']
        rpeaks.append(np.argmax(tup['signal'][a:b]) + a)

    rpeaks = sorted(list(set(rpeaks)))
    rpeaks = np.array(rpeaks, dtype='int')
    tup['rpeaks'] = rpeaks

    return tup
