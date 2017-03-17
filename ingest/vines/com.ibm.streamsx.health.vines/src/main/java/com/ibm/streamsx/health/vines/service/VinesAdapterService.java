/*******************************************************************************
 * Copyright (C) 2017 International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/
package com.ibm.streamsx.health.vines.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.streamsx.health.ingest.types.connector.PublishConnector;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.vines.VinesMessageParser;
import com.ibm.streamsx.health.vines.VinesParserResult;
import com.ibm.streamsx.health.vines.VinesToObservationParser;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.function.Function;
import com.ibm.streamsx.topology.spl.SPL;
import com.ibm.streamsx.topology.spl.SPLSchemas;

/**
 * Service for ingesting ViNES data from RabbitMQ.
 * 
 * Submission Time Parameters: 
 *  hostAndPort - specifies the host:port for the RabbitMQ server
 *  username - specifies the username to connect to RabbitMQ
 * 	 password - specifies the password to connect to RabbitMQ
 *  queue - specifies the RabbitMQ queue
 *  exchangeName - specifies the RabbitMQ exchange name. Default is "". 
 * 
 * Export Topic
 *  VINES_TOPIC = "ingest-vines"
 *  
 * Data Schema: 
 *  Data is exported as a ViNES Java object
 *  
 * 
 * @author streamsadmin
 *
 */
public class VinesAdapterService {
	
	public static final String VINES_TOPIC = "ingest-vines";
	public static final String VINES_ERROR_TOPIC = "ingest-vines-error";
	public static final String VINES_DEBUG_TOPIC = "ingest-vines-debug";
	
	private Topology topo;
	
	public VinesAdapterService() throws Exception {
		topo = new Topology("ViNESAdapter");
		
		topo.addClassDependency(Resources.class);
		topo.addClassDependency(NumberUtils.class);
		topo.addClassDependency(Observation.class);
		
		SPL.addToolkit(topo, new File(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streamsx.messaging"));
		SPL.addToolkit(topo, new File(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streamsx.json"));
		SPL.addToolkit(topo, new File("../../../common/com.ibm.streamsx.health.ingest"));
		
		Map<String, Object> rabbitMQParams = new HashMap<String, Object>();
		rabbitMQParams.put("hostAndPort", topo.createSubmissionParameter("hostAndPort", String.class));
		rabbitMQParams.put("exchangeName", topo.createSubmissionParameter("exchangeName", ""));
		rabbitMQParams.put("username", topo.createSubmissionParameter("username", String.class));
		rabbitMQParams.put("password", topo.createSubmissionParameter("password", String.class));
		rabbitMQParams.put("queueName", topo.createSubmissionParameter("queueName", String.class));
		rabbitMQParams.put("messageAttribute", SPLSchemas.STRING.getAttribute(0).getName());
		
		TStream<String> srcStream = SPL.invokeSource(topo, "com.ibm.streamsx.messaging.rabbitmq::RabbitMQSource", rabbitMQParams, SPLSchemas.STRING).toStringStream();
		TStream<VinesParserResult> parserStream = srcStream.transform(VinesMessageParser::fromJson).transform(new VinesToObservationParser());
		
		// Observation stream
		TStream<Observation> vinesStream = parserStream.multiTransform(new ObservationFunction());
		PublishConnector.publishObservation(vinesStream, VINES_TOPIC);
		
		// Error stream
		TStream<String> errStream = parserStream.transform(new ErrorFunction());
		errStream.publish(VINES_ERROR_TOPIC);

		// Debug stream
		srcStream.publish(VINES_DEBUG_TOPIC);
	}
	
	public Topology getTopology() {
		return topo;
	}
	
	public Object run(Type contextType, Map<String, Object> config) throws Exception {
		return StreamsContextFactory.getStreamsContext(contextType).submit(topo, config).get();
	}
	
	private static class ObservationFunction implements Function<VinesParserResult, Iterable<Observation>> {
		private static final long serialVersionUID = 1L;

		@Override
		public Iterable<Observation> apply(VinesParserResult parserResult) {
			return parserResult.getObservations();
		}
		
	}
	
	private static class ErrorFunction implements Function<VinesParserResult, String> {
		private static final long serialVersionUID = 1L;

		private static Gson gson = new Gson();
		
		@Override
		public String apply(VinesParserResult result) {
			if(result.getErrorMessages().size() > 0) {
				JsonObject jsonObj = new JsonObject();
				jsonObj.add("errorMessages", gson.toJsonTree(result.getErrorMessages()));
				jsonObj.add("rawMessage", gson.toJsonTree(result.getRawMessage()));
				
				return gson.toJson(jsonObj);
			}
			
			return null;
		} 
		
	}
}
