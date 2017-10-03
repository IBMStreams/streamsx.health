//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.connector;

import com.ibm.streamsx.health.fhir.internal.IdentityMapper;
import com.ibm.streamsx.health.fhir.model.ObxQueryParams;
import com.ibm.streamsx.health.fhir.model.ParseError;
import com.ibm.streamsx.health.ingest.types.connector.JsonPublisher;
import com.ibm.streamsx.health.ingest.types.connector.JsonSubscriber;
import com.ibm.streamsx.health.ingest.types.connector.JsonToModelConverter;
import com.ibm.streamsx.health.ingest.types.connector.ModelToJsonConverter;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.TopologyElement;
import com.ibm.streamsx.topology.json.JSONSchemas;
import com.ibm.streamsx.topology.spl.SPLStreams;

public class FhirObxConnector {
	
	public static void publish(TStream<ObxQueryParams> inputStream, String publishTopic) {
		mapAndPublish(inputStream, new IdentityMapper<ObxQueryParams>(), publishTopic);
	}
	
	public static void publishError(TStream<ParseError> inputStream, String publishTopic) {
		mapAndPublish(inputStream, new IdentityMapper<ParseError>(), publishTopic);
	}
	
	public static void publishDebug(TStream<String> inputStream, String publishTopic) {
		JsonPublisher.publish(inputStream, publishTopic);
	}
	
	@SuppressWarnings("rawtypes")
	private static <T> void mapAndPublish(TStream<T> inputStream, IdentityMapper<T> mapper, String publishTopic) {
		@SuppressWarnings("unchecked")
		TStream<String> jsonStream = inputStream.multiTransform(mapper).transform(new ModelToJsonConverter());
		JsonPublisher.publish(jsonStream, publishTopic);
	}

	public static TStream<ObxQueryParams> subscribe(TopologyElement te, String topic) {
		return SPLStreams.subscribe(te, topic, JSONSchemas.JSON)
				.transform(new JsonToModelConverter<ObxQueryParams>(ObxQueryParams.class));
	}
	
	public static TStream<ParseError> subscribeError(Topology te, String topic) {
		return SPLStreams.subscribe(te, topic, JSONSchemas.JSON)
				.transform(new JsonToModelConverter<ParseError>(ParseError.class));
				
	}
	
	public static TStream<String> subscribeDebug(Topology te, String topic) {
		return JsonSubscriber.subscribe(te, topic);
				
	}
}
