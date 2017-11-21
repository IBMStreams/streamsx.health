package com.ibm.streamsx.health.control.patientcontrolplane.api;

/**
 * Users should be implement this class and pass it to the PatientControlPlaneContext
 * in order to get notified whenever the list of patients associated with the 
 * current service are updated. 
 */
public interface ServicePatientListener {

	public void onUpdate(String oldValue, String newValue);
	
}
