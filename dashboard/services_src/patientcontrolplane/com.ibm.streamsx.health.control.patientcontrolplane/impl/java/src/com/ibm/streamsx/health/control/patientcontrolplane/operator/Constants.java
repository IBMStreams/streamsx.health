package com.ibm.streamsx.health.control.patientcontrolplane.operator;

import java.math.BigInteger;

import com.ibm.streamsx.health.control.patientcontrolplane.operator.ServiceInfo.Status;

public interface Constants {

	public static final String SERVICE_PATIENTS_CONTROL_VARIABLE_NAME = "servicePatients";
	public static final String GLOBAL_PATIENTS_CONTROL_VARIABLE_NAME = "globalPatients";
	public static final String UPDATED_PATIENT_ALERT_RULES_CONTROL_VARIABLE_NAME = "patientAlertRUles";
	public static final ServiceInfo STOPPED_SERVICE = new ServiceInfo(BigInteger.ZERO, "", Status.STOPPED, ServiceType.UNKNOWN);
}
