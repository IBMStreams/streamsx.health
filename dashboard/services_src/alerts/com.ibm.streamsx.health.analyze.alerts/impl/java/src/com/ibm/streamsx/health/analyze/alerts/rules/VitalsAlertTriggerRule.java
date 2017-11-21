package com.ibm.streamsx.health.analyze.alerts.rules;

import org.jeasy.rules.api.Facts;

import com.google.common.collect.Range;
import com.ibm.streamsx.health.analyze.alerts.rules.state.PatientRuleStateManager;
import com.ibm.streamsx.health.analyze.alerts.rules.state.RuleState;

public class VitalsAlertTriggerRule extends VitalsRangeRule {

	private Long duration;
	
	public VitalsAlertTriggerRule(String parentRuleName, String ruleId, String vitalName, Range<Double> range, Long duration) {
		super(parentRuleName, ruleId, VitalRuleAction.TRIGGER, vitalName, range);
		this.duration = duration;
	}

	@Override
	public boolean evaluate(Facts facts) {
		boolean evaluationResult = super.evaluate(facts);
		
		if(evaluationResult) {			
			VitalSample sample = (VitalSample)facts.get(VitalsFacts.VITALS_SAMPLE);
			RuleState state = PatientRuleStateManager.getInstance().getPatientRuleState(sample.getPatientId(), getParentRuleName());
			
			state.addAlertSample(sample);
			if(state.getDuration() >= duration) {
				return true;
			}			
		}
		
		return false;
	}

	@Override
	public void execute(Facts facts) throws Exception {
		String msg = "Alert TRIGGERED for '" + this.name + "'";
		facts.put(VitalsFacts.RULE_MESSAGE, msg);
	}
}
