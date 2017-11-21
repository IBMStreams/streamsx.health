package com.ibm.streamsx.health.control.patientcontrolplane.api;

/**
 * Users should be implement this class and pass it to the PatientControlPlaneContext
 * in order to get notified whenever there is a change to the patient's alerts. 
 */
public interface PatientAlertsListener {

	public void onUpdate(String oldValue, String newValue);
	
}
