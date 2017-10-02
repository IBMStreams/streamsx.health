//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;

import com.ibm.streamsx.health.fhir.mapper.ObxToSplMapper;
import com.ibm.streamsx.health.ingest.types.connector.PublishConnector;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.function.Supplier;

public class FhirObservationIngestService extends AbstractFhirService {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String FHIR_OBX_TOPIC="ingest-fhir-obx";

	public static void main(String[] args) {
		
		// TODO:  Provide properties
		String serverBase = "http://fhirtest.uhn.ca/baseDstu3";

		FhirObservationIngestService service = new FhirObservationIngestService(serverBase);
		service.run();
	}
	
	public FhirObservationIngestService(String serverBase) {
		super(serverBase);
	}

	@Override
	public void run() {
		Topology topology = new Topology("FhirObservationIngestService");
		ObxToSplMapper mapper = new ObxToSplMapper();
		ObservationQuery query = new ObservationQuery();

		addDependencies(topology);
		
		// TODO:  Make this pluggable

		TStream<String> patientIds = topology.periodicSource(new Supplier<String>() {

			@Override
			public String get() {
//				return "1c6299f7-2d06-4b5d-9efb-a8216a405a92";
				return "test-1796238";
			}
		}, 10, TimeUnit.SECONDS);
		
		patientIds.print();

		// query for observations
		TStream<BundleEntryComponent> bundleEntries = patientIds.multiTransform(patientId -> {
			return query.getObservations(getFhirClient(), patientId);
		});
		
		TStream<Observation> observations = bundleEntries.multiTransform(bundleEntry -> {
			return mapper.messageToModel(bundleEntry);
		});
		
		PublishConnector.publishObservation(observations, FHIR_OBX_TOPIC);
		
		observations.print();

		try {
			Map<String, Object> subProperties = new HashMap<>();
//			subProperties.put(ContextProperties.VMARGS, "-agentlib:jdwp=transport=dt_socket,suspend=y,server=y,address=127.0.0.1:7777");
			StreamsContextFactory.getStreamsContext(StreamsContext.Type.DISTRIBUTED).submit(topology, subProperties);
		} catch (Exception e) {
			TRACE.error("Unable to submit topology", e);
		}

	}

}
