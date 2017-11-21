package com.ibm.streamsx.health.control.patientcontrolplane.operator.adapter;

import java.util.Collection;

public interface RedisServerListener {

	public void patientsUpdated(Collection<String> patients);
	
	public void alertRulesUpdated(Collection<String> alertRules);
	
}
