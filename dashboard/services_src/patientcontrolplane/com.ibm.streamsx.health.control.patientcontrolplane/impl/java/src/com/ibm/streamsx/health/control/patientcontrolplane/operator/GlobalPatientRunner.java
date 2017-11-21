package com.ibm.streamsx.health.control.patientcontrolplane.operator;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;
import com.ibm.streamsx.health.control.patientcontrolplane.operator.adapter.ExternalServerAdapter;

public class GlobalPatientRunner implements Runnable {

	private String previousGlobalPatientsString;
	private BridgeControllable controller;
	private ExternalServerAdapter server;
	
	public GlobalPatientRunner(BridgeControllable controller, ExternalServerAdapter server) {
		this.controller = controller;
		this.server = server;
		previousGlobalPatientsString = "";
	}
	
	@Override
	public void run() {
		String currentGlobalPatientsString = controller.getGlobalPatientsCV().getValue();
		List<String> patientsToRemove = getPatientsToRemove(currentGlobalPatientsString);
		List<String> patientsToAdd = getPatientsToAdd(currentGlobalPatientsString);

		patientsToRemove.forEach(server::removePatientFromGlobalList);
		patientsToAdd.forEach(server::addPatientToGlobalList);
		
		previousGlobalPatientsString = currentGlobalPatientsString;
	}
	
	private List<String> getPatientsToRemove(String currentGlobalPatientsString) {
		List<String> currentGlobalPatients = new ArrayList<String>(Splitter.on(",").splitToList(currentGlobalPatientsString));
		List<String> previousGlobalPatients = new ArrayList<String>(Splitter.on(",").splitToList(previousGlobalPatientsString));
		
		// patients in previousGlobalPatients but NOT in globalPatients should be removed
		List<String> patientsToRemove = new ArrayList<String>();
		
		for(String patientId : previousGlobalPatients) {
			if(!currentGlobalPatients.contains(patientId)) {
				patientsToRemove.add(patientId);
			}
		}
		
		return patientsToRemove;
	}
	
	private List<String> getPatientsToAdd(String currentGlobalPatientsString) {
		List<String> currentGlobalPatients = new ArrayList<String>(Splitter.on(",").splitToList(currentGlobalPatientsString));
		List<String> previousGlobalPatients = new ArrayList<String>(Splitter.on(",").splitToList(previousGlobalPatientsString));
		
		List<String> patientsToAdd = new ArrayList<String>();
		for(String patientId : currentGlobalPatients) {
			if(!previousGlobalPatients.contains(patientId)) {
				patientsToAdd.add(patientId);
			}
		}
		
		return patientsToAdd;
	}
}
