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

import org.apache.log4j.Logger;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;

import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.health.fhir.connector.FhirObxConnector;
import com.ibm.streamsx.health.fhir.mapper.ObxToSplMapper;
import com.ibm.streamsx.health.fhir.model.ObxParseResult;
import com.ibm.streamsx.health.fhir.model.ObxQueryParams;
import com.ibm.streamsx.health.fhir.model.ParseError;
import com.ibm.streamsx.health.ingest.types.connector.PublishConnector;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.function.Supplier;

public class FhirObservationIngestService extends AbstractFhirService {

	public static Logger TRACE = Logger.getLogger(FhirObservationIngestService.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		FhirObservationIngestService service = new FhirObservationIngestService();
		service.run();
	}

	@Override
	public void run() {

		String streamContext = getStreamsContext();

		Topology topology = new Topology("FhirObservationIngestService");
		ObxToSplMapper mapper = new ObxToSplMapper();
		ObservationQuery query = new ObservationQuery();

		addDependencies(topology);

		String property = getProperties().getProperty(IServiceConstants.KEY_PERIOD);
		Long period = Long.valueOf(10);
		if (property != null && !property.isEmpty()) {
			try {
				period = Long.valueOf(property.trim());
			} catch (NumberFormatException e) {
				TRACE.error("period in service.properties cannot be converted to a number.");
				throw e;
			}
		}

		// Read list of patient ids from properties
		TStream<List<String>> properties = topology.periodicSource(new Supplier<List<String>>() {
			@Override
			public List<String> get() {
				String property = getProperties().getProperty(IServiceConstants.KEY_PATIENTIDS);
				if (property != null) {
					String[] pIds = property.split(",");
					ArrayList<String> ids = new ArrayList<String>();
					for (int i = 0; i < pIds.length; i++) {
						ids.add(pIds[i].trim());
					}
					return ids;
				}
				return null;
			}
		}, period, TimeUnit.SECONDS);

		// Transform patient ids to query parameters
		TStream<ObxQueryParams> fromProperties = properties.multiTransform(t -> {
			List<String> ids = t;
			List<ObxQueryParams> queries = new ArrayList<ObxQueryParams>();
			for (String id : ids) {
				queries.add(new ObxQueryParams().setPatientId(id));
			}
			return queries;
		});

		TStream<ObxQueryParams> patientQueries = null;

		if (streamContext.equals("DISTRIBUTED") || streamContext.equals("BUNDLE")) {
			// Alternatively, clients may publish patient id to the
			// FHIR_OBX_PATIENTIDS_TOPIC
			TStream<ObxQueryParams> fromSubscribe = FhirObxConnector.subscribe(topology,
					IServiceConstants.FHIR_OBX_PATIENTIDS_TOPIC);

			// Create a single stream for processing
			patientQueries = fromProperties.union(fromSubscribe);
		} else {
			patientQueries = fromProperties;
		}

		// query for observations
		TStream<BundleEntryComponent> bundleEntries = patientQueries.multiTransform(t -> {
			return query.getObservations(getFhirClient(), t);
		});

		// transform bundle entries to Observation
		TStream<ObxParseResult> parseResults = bundleEntries.transform(bundleEntry -> {
			return mapper.messageToModel(bundleEntry);
		});

		// unwind the observation list
		TStream<Observation> observations = parseResults.multiTransform(t -> {
			return t.getObservations();
		});

		// check if there is any error to report
		TStream<ParseError> parseError = parseResults.transform(t -> {
			if (t.getException() != null)
				return new ParseError().setException(t.getException().getMessage()).setRawMessage(t.getRawMessage());
			return null;
		});

		// Publish data stream for downstream services to analyze
		if (streamContext.equals("DISTRIBUTED") || streamContext.equals("BUNDLE")) {
			PublishConnector.publishObservation(observations, IServiceConstants.FHIR_OBX_TOPIC);
			FhirObxConnector.publishError(parseError, IServiceConstants.FHIR_OBX_ERROR_TOPIC);
			
			if (isDebug()) {
				TStream<String> rawMessage = parseResults.transform(t-> {
					return t.getRawMessage();
				});
				FhirObxConnector.publishDebug(rawMessage, IServiceConstants.FHIR_OBX_DEBUG_TOPIC);
			}
		}

		try {
			Map<String, Object> subProperties = new HashMap<>();

			String vmArgs = getVmArgs();

			if (vmArgs != null && !vmArgs.isEmpty()) {
				// Add addition VM Arguments as specified in properties file
				subProperties.put(ContextProperties.VMARGS, vmArgs);
			}

			if (isDebug())
				subProperties.put(ContextProperties.TRACING_LEVEL, TraceLevel.DEBUG);

			StreamsContextFactory.getStreamsContext(streamContext).submit(topology, subProperties);
		} catch (Exception e) {
			TRACE.error("Unable to submit topology", e);
		}

	}

}
