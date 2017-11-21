package com.ibm.streamsx.health.analyze.alerts.rules.state;

import java.util.HashMap;
import java.util.Map;

public class PatientRuleStateManager {

	private static PatientRuleStateManager instance;
	
	private Map<String /* patientId */, Map<String /* rulename */, RuleState>> patientStateMap;

	public static PatientRuleStateManager getInstance() {
		if(instance == null)
			instance = new PatientRuleStateManager();
		
		return instance;
	}
	
	private PatientRuleStateManager() {
		patientStateMap = new HashMap<String, Map<String,RuleState>>();
	}

	public RuleState getPatientRuleState(String patientId, String ruleName) {
		Map<String, RuleState> patientMap = patientStateMap.get(patientId);
		if(patientMap == null) {
			patientMap = new HashMap<String, RuleState>();
			patientStateMap.put(patientId, patientMap);
		}
		
		RuleState ruleState = patientMap.get(ruleName);
		if(ruleState == null) {
			ruleState = new RuleState();
			patientMap.put(ruleName, ruleState);
		}
		
		return ruleState;
	}
}
