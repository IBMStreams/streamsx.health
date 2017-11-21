package com.ibm.streamsx.health.control.patientcontrolplane.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Joiner;
import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.control.ControlPlaneContext;
import com.ibm.streams.operator.control.variable.ControlVariableAccessor;
import com.ibm.streamsx.health.control.patientcontrolplane.operator.Constants;

public class PatientControlPlaneContext {

	private static final Long INIT_DELAY = 3l;
	private static final Long PERIOD = 1l;
	
	private ControlPlaneContext cpc;
	private List<ServicePatientListener> serviceListeners;
	private List<PatientAlertsListener> alertListeners;
	private ControlVariableAccessor<String> servicePatientsCV;
	private ControlVariableAccessor<String> globalPatientsCV;
	private ControlVariableAccessor<String> alertEventCV;
	private String previousServicePatientsCVValue = "";
	
	private ControlVariableAccessor<String> updatedAlertRulesCV;
	private String previousPatientAlertsCVValue = "";
	
	public PatientControlPlaneContext(OperatorContext operatorContext) {
		this.cpc = operatorContext.getOptionalContext(ControlPlaneContext.class);
		this.cpc.connect();
		servicePatientsCV = this.cpc.createStringControlVariable(Constants.SERVICE_PATIENTS_CONTROL_VARIABLE_NAME, true, "");
		globalPatientsCV = this.cpc.createStringControlVariable(Constants.GLOBAL_PATIENTS_CONTROL_VARIABLE_NAME, true, "");
		updatedAlertRulesCV = this.cpc.createStringControlVariable(Constants.UPDATED_PATIENT_ALERT_RULES_CONTROL_VARIABLE_NAME, true, "");
		
		this.serviceListeners = new ArrayList<ServicePatientListener>();
		this.alertListeners = new ArrayList<PatientAlertsListener>();

		createServicePatientThread(operatorContext);
		createPatientAlertThread(operatorContext);
	}

	/**
	 * Used by the PatientDiscovery service to update the currently
	 * active set of patients
	 */
	public void updateGlobalPatientList(List<String> patientList) throws Exception {
		globalPatientsCV.setValue(Joiner.on(",").join(patientList));
	}

	public void addAlertEvent(String alertEvent) throws Exception {
		alertEventCV.setValue(alertEvent);
	}
	
	public String getUpdatedAlertRulesCVValue() {
		return updatedAlertRulesCV.getValue();
	}
	
	/**
	 * Adds a listener that gets notified whenever patients are 
	 * added to or removed from the service
	 */
	public void addServicePatientListener(ServicePatientListener listener) {
		this.serviceListeners.add(listener);
	}
	
	public void addPatientAlertsListener(PatientAlertsListener listener) {
		this.alertListeners.add(listener);
	}

	private void createPatientAlertThread(OperatorContext operatorContext) {
		ScheduledExecutorService executorService = operatorContext.getScheduledExecutorService();
        executorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				String currentCVValue = updatedAlertRulesCV.getValue();
				System.out.println("currentCVValue=" + currentCVValue);
				if(!currentCVValue.equals(previousPatientAlertsCVValue)) {
					for(PatientAlertsListener listener : alertListeners) {
						listener.onUpdate(previousPatientAlertsCVValue, currentCVValue);
					}
					previousPatientAlertsCVValue = currentCVValue;
				}
			}
        }, INIT_DELAY, PERIOD, TimeUnit.SECONDS);
	}
	
	private void createServicePatientThread(OperatorContext operatorContext) {
		ScheduledExecutorService executorService = operatorContext.getScheduledExecutorService();
        executorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				String currentCVValue = servicePatientsCV.getValue();
				if(!currentCVValue.equals(previousServicePatientsCVValue)) {
					for(ServicePatientListener listener : serviceListeners) {
						listener.onUpdate(previousServicePatientsCVValue, currentCVValue);
					}
					previousServicePatientsCVValue = currentCVValue;
				}
			}
        }, INIT_DELAY, PERIOD, TimeUnit.SECONDS);
	}
}
