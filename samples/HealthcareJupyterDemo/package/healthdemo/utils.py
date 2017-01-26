# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016, 2017
class DataAlreadyExistsError(RuntimeError):
    def __init__(self, label):
        self.message = str("Data with label '%s' already exists and cannot be added" % (label))

def get_patient_id(d):
    return d['patient']['identifier']

def get_index_by_label(d, label):
    for idx in range(len(d['data'])):
        if d['data'][idx]['label'] == label:
            return idx

    return None

def get_sampled_data_values(d, label):
    idx = get_index_by_label(d, label)
    return d['data'][idx]['valueSampledData']['values']

def get_coordinate_data_values(d, label):
    idx = get_index_by_label(d, label)
    return d['data'][idx]['valueCoordinateData']['values']

def get_period_value(d, label):
    idx = get_index_by_label(d, label)
    return d['data'][idx]['valueSampledData']['period']['value']

def get_sampled_data_unit(d, label):
    idx = get_index_by_label(d, label)
    return d['data'][idx]['valueSampledData']['unit']

def get_period_unit(d, label):
    idx = get_index_by_label(d, label)
    return d['data'][idx]['valueSampledData']['period']['unit']

def get_gain(d, label):
    idx = get_index_by_label(d, label)
    return d['data'][idx]['valueSampledData']['gain']

def get_initValue(d, label):
    idx = get_index_by_label(d, label)
    return d['data'][idx]['valueSampledData']['initVal']

def get_patient_ID(d):
    return d['patient']['identifier']

def add_sampled_data(d, label, sampled_data, period_value, period_unit, update_if_exists=False):
    # check if label already exists
    data_idx = get_index_by_label(d, label)
    if data_idx is not None:
        if update_if_exists == True:
            v = {'valuesSampledData' : { 'values' : sampled_data, 'period' : { 'value' : period_value, 'unit' : period_unit }}}
            d['data'][data_idx] = v
        else:
            raise DataAlreadyExistsError(label=label)
    else:
        v = {'label' : label, 'valuesSampledData' : { 'values' : sampled_data, 'period' : { 'value' : period_value, 'unit' : period_unit }}}
        d['data'].append(v)

def add_coordinate_data(d, label, coords, replace_if_exists=False):
    data_idx = get_index_by_label(d, label)
    if data_idx is not None:
        if replace_if_exists == True:
            v = {'valueCoordinateData' : {'values' : coords}}
            d['data'][data_idx] = v
        else:
            raise DataAlreadyExistsError(label=label)
    else:
        v = {'label' : label, 'valueCoordinateData' : {'values' : coords}}
        d['data'].append(v)
