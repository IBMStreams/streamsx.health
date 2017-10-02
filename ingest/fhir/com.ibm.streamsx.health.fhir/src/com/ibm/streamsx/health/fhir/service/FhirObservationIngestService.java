//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;

import com.ibm.streamsx.health.fhir.mapper.ObxToSplMapper;
import com.ibm.streamsx.health.fhir.service.model.ObxQueryParams;
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
	public static final String FHIR_OBX_PATIENTIDS_TOPIC="ingest-fhir-obx-patientIds";
	
	protected static final String KEY_PATIENTIDS = "patientids";
	protected static final String KEY_PERIOD = "period";


	public static void main(String[] args) {
		FhirObservationIngestService service = new FhirObservationIngestService();
		service.run();
	}

	@Override
	public void run() {
		Topology topology = new Topology("FhirObservationIngestService");
		ObxToSplMapper mapper = new ObxToSplMapper();
		ObservationQuery query = new ObservationQuery();

		addDependencies(topology);
		
		Object property = getProperties().get(KEY_PERIOD);
		Long period = Long.valueOf(10);
		if (property instanceof String && !((String)property).isEmpty()) {
			try {
				period = Long.valueOf(((String)property).trim());
			} catch (NumberFormatException e) {
				TRACE.error("period in service.properties cannot be converted to a number.");
				throw e;
			}
		}
		
		// Read list of patient ids from properties
		TStream<List<String>> properties = topology.periodicSource(new Supplier<List<String>>() {
			@Override
			public List<String> get() {
				Object property = getProperties().get(KEY_PATIENTIDS);
				if (property instanceof String)
				{
					String[] pIds = ((String)property).split(",");
					ArrayList<String> ids = new ArrayList<String>();
					for (int i = 0; i < pIds.length; i++) {
						ids.add(pIds[i].trim());
					}
					return ids;
				}
				return null;
			}
		}, period, TimeUnit.SECONDS);
		
		TStream<ObxQueryParams> fromProperties = properties.multiTransform(t->{
			List<String> ids = t;
			List<ObxQueryParams> queries = new ArrayList<ObxQueryParams>();
			for (String id : ids) {
				queries.add(new ObxQueryParams().setPatientId(id));
			}
			return queries;
		});
				
		// Alternatively, clients may publish patient id to the FHIR_OBX_PATIENTIDS_TOPIC
		// TODO:  This should be json
		TStream<ObxQueryParams> fromSubscribe = topology.subscribe(FHIR_OBX_PATIENTIDS_TOPIC, ObxQueryParams.class);

		// Create a single stream for processing
		TStream<ObxQueryParams> patientQueries = fromProperties.union(fromSubscribe);
				
		// query for observations
		TStream<BundleEntryComponent> bundleEntries = patientQueries.multiTransform(t -> {
			return query.getObservations(getFhirClient(), t);
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
