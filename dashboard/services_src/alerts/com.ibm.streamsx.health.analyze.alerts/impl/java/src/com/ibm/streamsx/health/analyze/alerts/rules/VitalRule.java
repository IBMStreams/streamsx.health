package com.ibm.streamsx.health.analyze.alerts.rules;

import org.jeasy.rules.core.BasicRule;

public abstract class VitalRule extends BasicRule {

	public static enum VitalRuleAction {
		TRIGGER,
		CANCEL;
	}
	
	private VitalRuleAction ruleAction;
	private String parentRuleName;
	
	public VitalRule(String parentRuleName, String ruleId, VitalRuleAction ruleAction) {
		super(ruleId);
		this.parentRuleName = parentRuleName;
		this.ruleAction = ruleAction;
	}

	public String getParentRuleName() {
		return parentRuleName;
	}
	
	public VitalRuleAction getRuleAction() {
		return ruleAction;
	}
}
