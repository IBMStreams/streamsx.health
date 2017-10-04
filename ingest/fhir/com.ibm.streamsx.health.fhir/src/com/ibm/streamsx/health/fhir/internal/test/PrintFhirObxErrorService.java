//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.internal.test;

import com.ibm.streamsx.health.fhir.connector.FhirObxConnector;
import com.ibm.streamsx.health.fhir.service.AbstractFhirService;
import com.ibm.streamsx.health.fhir.service.IServiceConstants;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;


/*
 * This is an internal service for testing the error connector
 */
public class PrintFhirObxErrorService extends AbstractFhirService{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		
		PrintFhirObxErrorService service = new PrintFhirObxErrorService();
		service.run();
		
	}

	@Override
	protected Topology createTopology() {
		Topology topology = new Topology("PrintFhirObxErrorService");

		TStream<String> rawMessages = FhirObxConnector.subscribeDebug(topology, IServiceConstants.FHIR_OBX_ERROR_TOPIC);
		rawMessages.print();
		
		return topology;
	}

}
