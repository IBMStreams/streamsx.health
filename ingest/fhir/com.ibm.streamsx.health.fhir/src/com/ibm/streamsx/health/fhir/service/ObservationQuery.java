//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Observation;

import com.ibm.streamsx.health.fhir.internal.PatientQueryMgr;
import com.ibm.streamsx.health.fhir.service.model.ObxQueryParams;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.param.DateRangeParam;

public class ObservationQuery implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private PatientQueryMgr queryManager = new PatientQueryMgr();

	List<BundleEntryComponent> getObservations(IGenericClient client, ObxQueryParams params) {
		
		System.out.println("Patient Query " + params);

		ArrayList<BundleEntryComponent> allEntries = new ArrayList<BundleEntryComponent>();
		// TODO: Handle date, and only fetch new result for a particular patient
		IQuery<?> query = client.search().forResource(Observation.class);

		// add patient id
		if (params.getPatientId() != null && !params.getPatientId().isEmpty())
			query = query.where(new ReferenceClientParam("subject").hasId(params.getPatientId()));

		// specify what to include
		query = query.include(new Include("Observation:Subject")).include(new Include("Observation:Device"))
				.include(new Include("Observation:Device:location"));
				
		// check page size
		if (params.getPageSize() > 0)
			query = query.count(params.getPageSize());

		// If no start and end time is provided, use information from
		// queryManager
		if (params.getStartTime() <= 0 || params.getEndTime() <= 0) {
			// First query will always get all
			// Subsequent query will get new observations since we last queried
			List<Long> range = queryManager.getTimeRange(params.getPatientId());
			if (range.get(0) != 0) {
				Date start = new Date(range.get(0));
				Date end = new Date(range.get(1));
				DateRangeParam dateRange = new DateRangeParam(start, end);
				
				System.out.println("DateRange: " + start + ":" + end);
				query = query.lastUpdated(dateRange);
			}
		}

		Bundle results = (Bundle) query.returnBundle(Bundle.class).execute();

		allEntries.addAll(results.getEntry());

		// Handle paging, keep querying until no more records can be found
		while (results.getLink(Bundle.LINK_NEXT) != null) {
			results = client.loadPage().next(results).execute();
			allEntries.addAll(results.getEntry());
		}

		System.out.println("Total number of entries: " + allEntries.size());

		return allEntries;
	}

	protected int getPageSize() {
		return 100;
	}

}
