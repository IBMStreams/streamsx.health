package com.ibm.streamsx.health.analyze.alerts.generator;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.streamsx.health.analyze.alerts.rules.VitalRules;
import com.ibm.streamsx.health.analyze.alerts.rules.VitalsAlertCancelRule;
import com.ibm.streamsx.health.analyze.alerts.rules.VitalsAlertTriggerRule;

public class VitalRulesGenerator {

	private static final String VITAL_NAME = "vitalName";
	private static final String RULE_NAME = "ruleName";
	private static final String MIN_INCLUSIVE = "minInclusive";
	private static final String MIN_EXCLUSIVE = "minExclusive";
	private static final String MAX_INCLUSIVE = "maxInclusive";
	private static final String MAX_EXCLUSIVE = "maxExclusive";
	private static final String DURATION = "duration";
	
	
	public static VitalRules generateRulesFromJson(String json) throws Exception {
		Gson gson = new Gson();
		JsonObject jsonObj = gson.fromJson(json, JsonObject.class);
		
		return generateRulesFromJson(jsonObj);
	}
	
	public static VitalRules generateRulesFromJson(JsonObject jsonObj) {
		try {
			validate(jsonObj);			
		} catch(Exception e) {
			e.printStackTrace();
		}

		VitalRules rules = new VitalRules();
		String vitalName = jsonObj.get(VITAL_NAME).getAsString();
		String ruleName = jsonObj.get(RULE_NAME).getAsString();
		Long duration = getDuration(jsonObj);
		Range<Double> range = getRange(jsonObj);
		
		// generate the rules to trigger the alert
		String triggerRuleId = String.format("%s [TRIGGER: %s]", ruleName, range.toString());
		VitalsAlertTriggerRule triggerRule = new VitalsAlertTriggerRule(ruleName, triggerRuleId, vitalName, range, duration);
		rules.register(triggerRule);
		
		// generate complement rules to cancel the alert
		RangeSet<Double> rangeSet = TreeRangeSet.create();
		rangeSet.add(range);
		RangeSet<Double> complement = rangeSet.complement();
		for(Range<Double> r : complement.asRanges()) {
			String cancelRuleId = String.format("%s [CANCEL: %s]", ruleName, r.toString());
			VitalsAlertCancelRule cancelRule = new VitalsAlertCancelRule(ruleName, cancelRuleId, vitalName, r, duration);
			rules.register(cancelRule);
		}
		
		return rules;
	}

	private static Range<Double> getRange(JsonObject jsonObj) {
		BoundType lowerType = getLowerType(jsonObj); 
		BoundType upperType = getUpperType(jsonObj);
		Double lowerValue = getLowerValue(jsonObj);
		Double upperValue = getUpperValue(jsonObj);

		if(upperValue.equals(Double.POSITIVE_INFINITY)) {
			return Range.downTo(lowerValue, lowerType);
		} else if(lowerValue.equals(Double.NEGATIVE_INFINITY)) {
			return Range.upTo(upperValue, upperType);
		} else {
			return Range.range(lowerValue, lowerType, upperValue, upperType);	
		}
	}
	
	private static Long getDuration(JsonObject jsonObj) {
		return jsonObj.has(DURATION) ? jsonObj.get(DURATION).getAsLong() : 0;
	}
	
	private static Double getUpperValue(JsonObject jsonObj) {
		if(jsonObj.has(MAX_EXCLUSIVE))
			return jsonObj.get(MAX_EXCLUSIVE).getAsDouble();
		else if(jsonObj.has(MAX_INCLUSIVE))
			return jsonObj.get(MAX_INCLUSIVE).getAsDouble();
		else
			return Double.POSITIVE_INFINITY;
	}

	private static Double getLowerValue(JsonObject jsonObj) {
		if(jsonObj.has(MIN_EXCLUSIVE))
			return jsonObj.get(MIN_EXCLUSIVE).getAsDouble();
		else if(jsonObj.has(MIN_INCLUSIVE))
			return jsonObj.get(MIN_INCLUSIVE).getAsDouble();
		else
			return Double.NEGATIVE_INFINITY;
	}

	private static BoundType getUpperType(JsonObject jsonObj) {
		return jsonObj.has(MIN_INCLUSIVE) ? BoundType.CLOSED : BoundType.OPEN;
	}

	private static BoundType getLowerType(JsonObject jsonObj) {
		return jsonObj.has(MAX_INCLUSIVE) ? BoundType.CLOSED : BoundType.OPEN;
	}

	private static void validate(JsonObject jsonObj) throws Exception {
		if(!jsonObj.has("vitalName")) {
			throw new Exception("JSON must contain 'vitalName' member.");
		}
		
		if(!jsonObj.has("ruleName")) {
			throw new Exception("JSON must contain 'ruleName' member.");
		}		
		
		if(jsonObj.has(MIN_INCLUSIVE) && jsonObj.has(MIN_EXCLUSIVE)) {
			throw new Exception("JSON can only contain one of the following members: 'minInclusive' or 'minExclusive'.");
		}
		
		if(jsonObj.has(MAX_INCLUSIVE) && jsonObj.has(MAX_EXCLUSIVE)) {
			throw new Exception("JSON can only contain one of the following members: 'maxInclusive' or 'maxExclusive'.");
		}
	}
	
}
