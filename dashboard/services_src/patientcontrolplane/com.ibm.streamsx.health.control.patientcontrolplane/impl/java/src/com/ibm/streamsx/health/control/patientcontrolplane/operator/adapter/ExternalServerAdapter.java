package com.ibm.streamsx.health.control.patientcontrolplane.operator.adapter;

import java.util.Collection;
import java.util.List;

import com.ibm.streamsx.health.control.patientcontrolplane.operator.ServiceInfo;

public interface ExternalServerAdapter {

	/**
	 * Sets the name of the service. Subclasses are encouraged to call this method
	 * from the constructor. 
	 * in the constructor  
	 * @param serviceName
	 */
	public void setServiceName(String serviceName);
	
	/**
	 * Registers the service with the backend server
	 * @param serviceName the name of the service
	 * @param serviceInfo the serviceInfo related to the service
	 */
	public void registerService(ServiceInfo info);
	
	/**
	 * Unregisters the service with the backend server
	 * @param serviceName the name of the service
	 */
	public void unregisterService();
	
	/**
	 * Register the service's output topics
	 * @param topics the topics to register
	 */
	public void registerServiceTopics(List<String> topics);

	/**
	 * Add a patient to the global list of patients that should be monitored
	 * (should only be used by the PatientDiscoveryService)
	 * @param patientId
	 */
	public void addPatientToGlobalList(String patientId);
	
	/**
	 * Remove a patient from the global list of patients that should be monitored
	 * (should only be used by the PatientDiscoveryService)
	 * @param patientId
	 */
	public void removePatientFromGlobalList(String patientId);

	/**
	 * Remove all patients from the global list of patients that should be monitored
	 * (should only be used by the PatientDiscoveryService)
	 */
	public void removeAllPatientsFromGlobalList();
	
	/**
	 * Return the patient list for this service.
	 * @return the list of patients that are enabled for this service.
	 */
	public Collection<String> getServicePatients();	
	
	/**
	 * Return a list of all alerts.
	 * @return the list of all alerts.
	 */
	public Collection<String> getAlerts();
	
	/**
	 * Closes any active connections. 
	 */
	public void close();
}
