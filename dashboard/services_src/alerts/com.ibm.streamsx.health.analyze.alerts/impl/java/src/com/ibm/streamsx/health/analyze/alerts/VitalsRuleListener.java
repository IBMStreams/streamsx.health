package com.ibm.streamsx.health.analyze.alerts;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.RuleListener;

import com.ibm.streams.operator.Tuple;
import com.ibm.streamsx.health.analyze.alerts.rules.VitalRule;
import com.ibm.streamsx.health.analyze.alerts.rules.VitalSample;
import com.ibm.streamsx.health.analyze.alerts.rules.VitalsFacts;
import com.ibm.streamsx.health.analyze.alerts.rules.VitalRule.VitalRuleAction;

public class VitalsRuleListener implements RuleListener {

	private AlertManager alertManager;
	
	public VitalsRuleListener(AlertManager alertManager) {
		this.alertManager = alertManager;
	}
	
	@Override
	public void onSuccess(Rule rule, Facts facts) {
		VitalRule vitalRule = (VitalRule)rule;
		VitalSample sample = (VitalSample)facts.get(VitalsFacts.VITALS_SAMPLE);
		Tuple inputTuple = (Tuple)facts.get(VitalsFacts.INPUT_TUPLE);
		Tuple readingTuple = ((Tuple)facts.get(VitalsFacts.INPUT_TUPLE)).getTuple("reading");
		String patientId = sample.getPatientId();
		
		boolean isAlertTriggeredForRule = alertManager.isAlertTriggeredForRule(patientId, vitalRule);
		Long epochSeconds = readingTuple.getLong("ts");
		
		String msg = (String)facts.get(VitalsFacts.RULE_MESSAGE);
		if(isAlertTriggeredForRule && vitalRule.getRuleAction() == VitalRuleAction.CANCEL) {
			alertManager.cancelAlert(patientId, vitalRule, epochSeconds, inputTuple, msg);
		} else if(!isAlertTriggeredForRule && vitalRule.getRuleAction() == VitalRuleAction.TRIGGER) {
			alertManager.triggerAlert(patientId, vitalRule, epochSeconds, inputTuple, msg);
		}
	}
	
	@Override
	public void onFailure(Rule arg0, Facts arg1, Exception e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	}
	
	@Override
	public boolean beforeEvaluate(Rule rule, Facts facts) {
		return true;
	}

	@Override
	public void afterEvaluate(Rule rule, Facts facts, boolean evaluationResult) { 
		//System.out.println("AFTER EVALUATE: rule=" + rule.getName() + ", result=" + evaluationResult);
	}

	@Override
	public void beforeExecute(Rule rule, Facts facts) { }
}
