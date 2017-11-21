package com.ibm.streamsx.health.analyze.alerts.rules;

import java.util.Map;

public class VitalSample {

	private String patientId;
	private Long epochSeconds;
	private Map<String, Double> vitalsMap;
	
	public VitalSample(String patientId, Long epochSeconds, Map<String, Double> vitalsMap) {
		this.patientId = patientId;
		this.epochSeconds = epochSeconds;
		this.vitalsMap = vitalsMap;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public Long getEpochSeconds() {
		return epochSeconds;
	}

	public void setEpochSeconds(Long epochSeconds) {
		this.epochSeconds = epochSeconds;
	}

	public Map<String, Double> getVitalsMap() {
		return vitalsMap;
	}
	
	@Override
	public String toString() {
		return "VitalSample [patientId=" + patientId + ", epochSeconds=" + epochSeconds + ", vitalsMap=" + vitalsMap
				+ "]";
	}
}
