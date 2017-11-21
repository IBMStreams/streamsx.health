package com.ibm.streamsx.health.analyze.alerts.rules;

import java.util.Map;

import org.jeasy.rules.api.Facts;

import com.google.common.collect.Range;

public class VitalsRangeRule extends VitalRule {

	private Range<Double> range;
	private String vitalName;
	
	public VitalsRangeRule(String parentRuleName, String ruleName, VitalRuleAction ruleAction, String vitalName, Range<Double> range) {
		super(parentRuleName, ruleName, ruleAction);
		this.vitalName = vitalName;
		this.range = range;
	}
	
	@Override
	public boolean evaluate(Facts facts) {
		VitalSample sample = (VitalSample)facts.get(VitalsFacts.VITALS_SAMPLE);
		Map<String, Double> vitalsMap = sample.getVitalsMap();
				
		// return true if the map contains the specified vital
		// and the value of the vital falls within the range
		return vitalsMap.containsKey(vitalName) && 
				range.contains(vitalsMap.get(vitalName));
	}
}
