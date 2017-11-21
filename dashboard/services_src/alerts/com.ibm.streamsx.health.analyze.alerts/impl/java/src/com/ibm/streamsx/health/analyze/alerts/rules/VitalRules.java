package com.ibm.streamsx.health.analyze.alerts.rules;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;

public class VitalRules extends Rules {

	private Map<String /* ruleId */, Rule> ruleMap;
	
	public VitalRules() {
		ruleMap = new ConcurrentHashMap<String, Rule>();
	}

	public void registerAll(VitalRules rules) {
		Iterator<Rule> it = rules.iterator();
		while(it.hasNext())
			register(it.next());
	}
	
	@Override
	public void register(Object rule) {
		super.register(rule);		
		
		if(rule instanceof Rule) {
			ruleMap.put(((Rule)rule).getName(), (Rule)rule);
		}
	}

	@Override
	public void unregister(Object rule) {
		super.unregister(rule);
		
		if(rule instanceof Rule) {
			ruleMap.remove(((Rule)rule).getName());
		}
	}
	
	public boolean contains(Rule rule) {
		return ruleMap.containsKey(rule.getName());
	}
}
