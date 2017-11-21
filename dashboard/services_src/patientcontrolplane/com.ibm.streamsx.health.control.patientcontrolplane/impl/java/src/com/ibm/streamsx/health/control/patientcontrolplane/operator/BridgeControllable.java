package com.ibm.streamsx.health.control.patientcontrolplane.operator;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.control.ControlPlaneContext;
import com.ibm.streams.operator.control.Controllable;
import com.ibm.streams.operator.control.variable.ControlVariableAccessor;

/*
 * Handles communication between the operator and 
 * the JCP. 
 */
public class BridgeControllable implements Controllable {
	private ControlVariableAccessor<String> servicePatientsCV;
	private ControlVariableAccessor<String> globalPatientsCV;
	private ControlVariableAccessor<String> alertRulesCV;
	private ControlPlaneContext cpc;
	private AtomicBoolean isConnected;
	
	public BridgeControllable(ControlPlaneContext cpc) {
		this.cpc = cpc;
		isConnected = new AtomicBoolean(false);
	}
	
	@Override
	public void event(MBeanServerConnection jcp, OperatorContext context, EventType eventType) {
		System.out.println("event: " + eventType);
	}

	@Override
	public boolean isApplicable(OperatorContext context) {
		return true;
	}

	@Override
	public void setup(MBeanServerConnection jcp, OperatorContext context) throws InstanceNotFoundException, Exception {
		System.out.println("setup() called");
		isConnected.set(true);
		servicePatientsCV = cpc.createStringControlVariable(Constants.SERVICE_PATIENTS_CONTROL_VARIABLE_NAME, true, "");
		globalPatientsCV = cpc.createStringControlVariable(Constants.GLOBAL_PATIENTS_CONTROL_VARIABLE_NAME, true, "");		
		alertRulesCV = cpc.createStringControlVariable(Constants.UPDATED_PATIENT_ALERT_RULES_CONTROL_VARIABLE_NAME, true, "");
		
	}
	
	public ControlVariableAccessor<String> getServicePatientsCV() {
		return servicePatientsCV;
	}

	public ControlVariableAccessor<String> getGlobalPatientsCV() {
		return globalPatientsCV;
	}

	public ControlVariableAccessor<String> getUpdatedAlertRulesCV() {
		return alertRulesCV;
	}
	
	public boolean isConnected() {
		return isConnected.get();
	}
}
