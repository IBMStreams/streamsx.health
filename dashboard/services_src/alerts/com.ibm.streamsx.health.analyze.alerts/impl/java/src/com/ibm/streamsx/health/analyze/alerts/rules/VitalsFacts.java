package com.ibm.streamsx.health.analyze.alerts.rules;

import java.util.HashMap;
import java.util.Map;

import org.jeasy.rules.api.Facts;

import com.ibm.streams.operator.Tuple;

public class VitalsFacts extends Facts {

	public static final String INPUT_TUPLE = "inputTuple";
	public static final String VITALS_SAMPLE = "vitalsSample";
	public static final String RULE_MESSAGE = "ruleMessage";
	
	public static Facts newVitalsFacts(String patientId, Tuple tuple) {		
		Tuple readingTuple = tuple.getTuple("reading");
		Tuple readingTypeTuple = readingTuple.getTuple("readingType");

		Long epochSeconds = readingTuple.getLong("ts");
		String vitalName = readingTypeTuple.getString("code");
		Double value = readingTuple.getDouble("value");
		
		Map<String, Double> vitalsMap = new HashMap<String, Double>();
		vitalsMap.put(vitalName, value);
		    	
    	VitalSample sample = new VitalSample(patientId, epochSeconds, vitalsMap);
    	
		Facts facts = new Facts();
		facts.put(VITALS_SAMPLE, sample);
		facts.put(INPUT_TUPLE, tuple);
		
		return facts;
	}
	
//	private static Map<String, Double> fixMap(Map<?, ?> vitalsMap) {
//    	Map<String, Double> fixedMap = new HashMap<String, Double>();
//    	vitalsMap.forEach((key, value) -> {
//    		if(key instanceof RString && value instanceof Double) {
//    			fixedMap.put(((RString)key).getString(), (Double)value);
//    		}
//    	});
//    	
//    	return fixedMap;
//    }
}
