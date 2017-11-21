package com.ibm.streamsx.health.analyze.alerts.rules.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.streamsx.health.analyze.alerts.rules.VitalSample;

public class RuleState {

	private List<VitalSample> sampleWindow;
	private boolean lastSampleIsAlertSample;
	
	public RuleState() {
		sampleWindow = new ArrayList<VitalSample>();
		lastSampleIsAlertSample = false;
	}
	
	public void addAlertSample(VitalSample sample) {
		if(!lastSampleIsAlertSample) {
			sampleWindow.clear();
			lastSampleIsAlertSample = true;
		}
		
		sampleWindow.add(sample);
	}
	
	public void addNonAlertSample(VitalSample sample) {
		if(lastSampleIsAlertSample) {
			sampleWindow.clear();
			lastSampleIsAlertSample = false;
		}
		
		sampleWindow.add(sample);
	}

	public Long getDuration() {
		return sampleWindow.size() > 1 ? sampleWindow.get(sampleWindow.size()-1).getEpochSeconds() - sampleWindow.get(0).getEpochSeconds() : 0;
	}
	
	public boolean isLastSampleIsAlertSample() {
		return lastSampleIsAlertSample;
	}
}
