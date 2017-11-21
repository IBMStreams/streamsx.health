package com.ibm.streamsx.health.analyze.alerts.generator;

public class JsonRule {

	private String ruleName;
	private String vitalName;
	private String range;
	private Long duration;

	public String getVitalName() {
		return vitalName;
	}
	
	public Long getDuration() {
		return duration;
	}
	
	public String getRange() {
		return range;
	}
	
	public String getRuleName() {
		return ruleName;
	}
	
}
