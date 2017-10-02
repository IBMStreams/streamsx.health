//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Observation;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;

public class ObservationQuery implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	List<BundleEntryComponent> getObservations(IGenericClient client, String patientId) {
		ArrayList<BundleEntryComponent> allEntries = new ArrayList<BundleEntryComponent>();
		// TODO: Handle date, and only fetch new result for a particular patient
		Bundle results = client.search().forResource(Observation.class)
				.where(new ReferenceClientParam("subject").hasId(patientId)).include(new Include("Observation:Subject"))
				.include(new Include("Observation:Device")).include(new Include("Observation:Device:location")).count(getPageSize())
				.returnBundle(Bundle.class).execute();

		allEntries.addAll(results.getEntry());

		// Handle paging, keep querying until no more records can be found
		while (results.getLink(Bundle.LINK_NEXT) != null) {
			results = client.loadPage().next(results).execute();
			allEntries.addAll(results.getEntry());
		}
		
		System.out.println("Total number of entries: " + allEntries.size());

		return allEntries;
	}
	
	protected int getPageSize()
	{
		return 100;
	}

}
