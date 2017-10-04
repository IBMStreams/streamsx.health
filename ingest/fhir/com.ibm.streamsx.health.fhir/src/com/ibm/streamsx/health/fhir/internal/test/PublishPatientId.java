//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.internal.test;

import java.util.concurrent.TimeUnit;

import com.ibm.streamsx.health.fhir.connector.FhirObxConnector;
import com.ibm.streamsx.health.fhir.model.ObxQueryParams;
import com.ibm.streamsx.health.fhir.service.AbstractFhirService;
import com.ibm.streamsx.health.fhir.service.IServiceConstants;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.function.Supplier;

public class PublishPatientId extends AbstractFhirService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		PublishPatientId service = new PublishPatientId();
		service.run();
	}

	@Override
	protected Topology createTopology() {
		
		Topology topology = new Topology("PublishPatientId");
		addDependencies(topology);
		
		TStream<ObxQueryParams> patientIds = topology.periodicSource(new Supplier<ObxQueryParams>() {

			@Override
			public ObxQueryParams get() {
				return new ObxQueryParams().setStartTime(1500350400).setEndTime(1500436800).setPatientId("191025");
			}
		}, 10, TimeUnit.SECONDS);
		
		FhirObxConnector.publish(patientIds, IServiceConstants.FHIR_OBX_PATIENTIDS_TOPIC);
		
		return topology;
	}

}
