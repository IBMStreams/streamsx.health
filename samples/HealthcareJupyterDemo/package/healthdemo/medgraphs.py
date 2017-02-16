# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016, 2017
import numpy as np
from bokeh.plotting import figure, show
from bokeh.layouts import column, row
from bokeh.models import Range1d, BasicTicker
from bokeh.io import output_notebook, push_notebook
from bokeh.models.widgets import Div, Paragraph
import time
import collections
import queue
from .utils import *

class NumericText:
    def __init__(self, signal_label, title, color, override_str=None):
        self.sig_label = signal_label
        self.data_queue = queue.Queue(maxsize=1000)
        self.div = Div(text='-1', width=280, height=120)
        self.title = title
        self.unit = None
        self.color = color

        self.div_str = '<div style=\"background-color: #152935; border: 3px solid black; width: 280px; height: 120px; font-family: arial; color: {0}\"> <div style=\"font-size: 20px; margin-left: 5px; margin-top: 20px; margin-bottom: 0px\"> {1} </div> <div style=\"font-size: 80px; text-align: center; margin-bottom: 0px\"> {2} </div> <div style=\"text-align: right; font-size: 30px; margin-right:5px\"> {3} </div> </div>'
        if override_str is not None:
            self.div_str = override_str

    def _format_text(self, value):
       return str(self.div_str.format(self.color, self.title, int(value), self.unit))

    def get_figure(self):
        return self.div

    def add(self, d):
        if self.unit == None:
            self.unit = get_sampled_data_unit(d, self.sig_label)

        data_points = get_sampled_data_values(d, self.sig_label)
        list = [self.data_queue.put(data) for data in data_points]

    def update(self):
        try:
            value = self.data_queue.get_nowait()
        except queue.Empty:
            return

        self.div.text = self._format_text(value)

class ABPNumericText(NumericText):
    def __init__(self, abp_sys_label, abp_dia_label, title, color, override_str=None):
        self.abp_sys_label = abp_sys_label
        self.abp_dia_label = abp_dia_label
        self.data_queue = queue.Queue(maxsize=1000)
        self.div = Div(text='-1', width=280, height=120)
        self.title = title
        self.unit = None
        self.color = color

        self.div_str = '<div style=\"background-color: #152935; border: 3px solid black; width: 280px; height: 120px; font-family: arial; color: {0}\"> <div style=\"font-size: 20px; margin-left: 5px; margin-top: 20px; margin-bottom: 0px; \"> {1} </div> <div style=\"font-size: 50px; text-align: center; margin-top: -10px; margin-bottom: 0px; line-height: 50px\"> {2} </div> <div style=\"text-align: right; font-size: 30px; margin-top: 0px; margin-right:5px\"> {3} </div> </div>'

        if override_str is not None:
            self.div_str = override_str

    def _format_text(self, value):
           return str(self.div_str.format(self.color, self.title, value, self.unit))

    def add(self, d):
        if self.unit == None:
            self.unit = get_sampled_data_unit(d, self.abp_sys_label)

        sys_points = get_sampled_data_values(d, self.abp_sys_label)
        dia_points = get_sampled_data_values(d, self.abp_dia_label)

        if len(sys_points) != len(dia_points):
            return

        for idx in range(len(sys_points)):
            if sys_points[idx] < 0 or dia_points[idx] < 0:
                continue
            self.data_queue.put(str('%s/%s' % (int(sys_points[idx]), int(dia_points[idx]))))



class PoincareGraph:
    def __init__(self, signal_label, title='Poincare', plot_width=240, plot_height=240):
        self.sig_label = signal_label
        self.data_queue = queue.Queue(maxsize=1000)
        self.pc_xdata = []
        self.pc_ydata = []
        self.poincare = figure(plot_width=plot_width, plot_height=plot_height, toolbar_location=None)
        self.poincare.line([0, 2], [0, 2], line_width=1, line_color="black")
        self.poincare.title.text = "Poincaré plot"
        self.poincare.title.align = "center"
        self.poincare.xaxis.axis_label = "RR(n)"
        self.poincare.yaxis.axis_label = "RR(n+1)"

        self.poincare_graph = self.poincare.circle(self.pc_xdata, self.pc_ydata, size=5, color="navy", alpha=0.5)
        self.poincare.x_range = Range1d(start=0.5, end=1.0)
        self.poincare.y_range = Range1d(start=0.5, end=1.0)

    def get_figure(self):
        return self.poincare

    def add(self, d):
        data_points = get_coordinate_data_values(d, self.sig_label)
        list = [self.data_queue.put(data) for data in data_points]

    def update(self):
        try:
            point = self.data_queue.get_nowait()
        except queue.Empty:
            return

        #self.pc_xdata.append(point[0])
        #self.pc_ydata.append(point[1])
        new_data = {'x' : [point[0]], 'y' : [point[1]]}
        self.poincare_graph.data_source.stream(new_data)

class ECGGraph:
    def __init__(self, signal_label, title='ECG', min_range=-1, max_range=1, plot_width=425, plot_height=200):
        self.data_queue = queue.Queue(maxsize=1000)
        self.ts_queue = queue.Queue(maxsize=1000)
        self.sig_label = signal_label

        self.ecg_patient_id = None
        self.ecg_maxLength = 600
        self.ecg_time = 0
        self.ecg_xdata = []
        self.ecg_ydata = []
        #self.ecg_xdata = collections.deque(maxlen=self.ecg_maxLength)
        #self.ecg_ydata = collections.deque(maxlen=self.ecg_maxLength)

        self.ecgFig = figure(plot_width=plot_width, plot_height=plot_height, toolbar_location=None)
        self.ecgFig.title.text = title
        self.ecgFig.title.align = "center"
        self.ecgFig.outline_line_width = 1
        self.ecgFig.outline_line_color = "black"
        self.ecgFig.y_range = Range1d(start=min_range, end=max_range)
        self.ecgFig.ygrid.grid_line_color = "red"
        self.ecgFig.ygrid.grid_line_alpha = 0.3
        self.ecgFig.xgrid.grid_line_color = "red"
        self.ecgFig.xgrid.grid_line_alpha = 0.3
        self.ecgFig.ygrid.minor_grid_line_color = 'red'
        self.ecgFig.ygrid.minor_grid_line_alpha = 0.1
        self.ecgFig.xgrid.minor_grid_line_color = 'red'
        self.ecgFig.xgrid.minor_grid_line_alpha = 0.1
        self.ecgFig.xaxis.major_label_text_font_size = '0pt'

        ## at a frequency of 100Hz, every 20 point is equal to 0.2 sec
        ## and ECG chart has a major tick every 0.2 sec, with minor
        ## ticks every 0.04 sec (5 minor ticks per major tick)
        xticker = BasicTicker()
        xticker.desired_num_ticks = int(self.ecg_maxLength / 20)
        xticker.num_minor_ticks = 5
        self.ecgFig.xgrid.ticker = xticker

        self.ecgGraph = self.ecgFig.line(self.ecg_xdata, self.ecg_ydata, line_width=1, line_color="black")

        self.initialData = True
        self.period_value = 0
        self.initValue = 0
        self.gain = 1

    def get_figure(self):
        return self.ecgFig

    def add(self, d):
        if self.initialData == True:
            self.period_value = get_period_value(d, self.sig_label)
            self.gain = get_gain(d, self.sig_label)
            self.initValue = get_initValue(d, self.sig_label)

            if self.ecg_patient_id is None:
                self.ecg_patient_id = get_patient_ID(d)
                self.ecgFig.title.text += str(" (PatientID: %s)" % (self.ecg_patient_id))
            self.initialData = False

        data_points = get_sampled_data_values(d, self.sig_label)
        ts_points = get_sampled_data_values(d, 'timestamp')

        list = [self.data_queue.put(data) for data in data_points]

    def update(self):
        try:
            point_val = self.data_queue.get_nowait()
        except queue.Empty:
            return

        point_val = (point_val)/self.gain
        #point_ts = ts_points[idx]
        point_ts = self.ecg_time

        ## update ECG graph data
        #self.ecg_xdata.append(point_ts)
        #self.ecg_ydata.append(point_val)
        #self.ecgGraph.data_source.data["x"] = self.ecg_xdata
        #self.ecgGraph.data_source.data["y"] = self.ecg_ydata

        new_data = {'x' : [point_ts], 'y' : [point_val]}
        self.ecgGraph.data_source.stream(new_data, rollover=self.ecg_maxLength)

        self.ecg_time += 1
