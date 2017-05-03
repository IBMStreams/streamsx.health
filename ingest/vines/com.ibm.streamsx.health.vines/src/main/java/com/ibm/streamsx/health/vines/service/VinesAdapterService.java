/*******************************************************************************
 * Copyright (C) 2017 International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/
package com.ibm.streamsx.health.vines.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.math.NumberUtils;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.health.ingest.types.connector.PublishConnector;
import com.ibm.streamsx.health.ingest.types.connector.SubscribeConnector;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.vines.VinesMessageParser;
import com.ibm.streamsx.health.vines.VinesParserResult;
import com.ibm.streamsx.health.vines.VinesToObservationParser;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.function.Function;
import com.ibm.streamsx.topology.function.Supplier;
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
		topo.addClassDependency(CommandLineParser.class);
		
		SPL.addToolkit(topo, new File(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streamsx.messaging"));
		SPL.addToolkit(topo, new File(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streamsx.json"));
		
		Map<String, Object> rabbitMQParams = new HashMap<String, Object>();
		rabbitMQParams.put("hostAndPort", topo.createSubmissionParameter("hostAndPort", String.class));
		rabbitMQParams.put("exchangeName", topo.createSubmissionParameter("exchangeName", ""));
		rabbitMQParams.put("username", topo.createSubmissionParameter("username", String.class));
		rabbitMQParams.put("password", topo.createSubmissionParameter("password", String.class));
		rabbitMQParams.put("queueName", topo.createSubmissionParameter("queueName", String.class));
		rabbitMQParams.put("messageAttribute", SPLSchemas.STRING.getAttribute(0).getName());
		Supplier<Boolean> mappingEnabledSubmissionParam = topo.createSubmissionParameter("mappingEnabled", Boolean.class);
		
		TStream<String> srcStream = SPL.invokeSource(topo, "com.ibm.streamsx.messaging.rabbitmq::RabbitMQSource", rabbitMQParams, SPLSchemas.STRING).toStringStream();
		TStream<VinesParserResult> parserStream = srcStream
				.transform(VinesMessageParser::fromJson)
				.transform(new VinesToObservationParser(mappingEnabledSubmissionParam));
		
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
	
	public static void main(String[] args) throws Exception {
		Option contextOption = Option.builder("c")
									 .longOpt("context-type")
									 .hasArg()
									 .argName("context type")
									 .required()
									 .build();
		
		Option hostOption = Option.builder("h")
								  .longOpt("host")
								  .hasArg()
								  .argName("host")
								  .required()
								  .build();
		
		Option portOption = Option.builder("p")
								  .longOpt("port")
								  .hasArg()
								  .argName("port")
								  .required()
								  .build();
		
		Option usernameOption = Option.builder("u")
									  .longOpt("username")
									  .hasArg()
									  .argName("username")
									  .required()
									  .build();
		
		Option passwordOption = Option.builder("P")
									  .longOpt("password")
									  .hasArg()
									  .argName("password")
									  .required()
									  .build();
		
		Option queueOption = Option.builder("q")
								   .longOpt("queue")
								   .hasArg()
								   .argName("queue")
								   .required()
								   .build();
		
		Option exchangeOption = Option.builder("e")
								      .longOpt("exchange")
								      .hasArg()
								      .argName("exchange name")
								      .required()
								      .build();
		
		Option debugOption = Option.builder("d")
								   .longOpt("debug")
								   .hasArg()
								   .argName("isDebugEnabled")
								   .required(false)
								   .type(Boolean.class)
								   .build();
		
		Option mappingEnabledOption = Option.builder("m")
									.longOpt("mapping-enabled")
									.argName("isMappingEnabled")
									.required(true)
									.type(Boolean.class)
									.build();
		
		Options options = new Options();
		options.addOption(contextOption);
		options.addOption(hostOption);
		options.addOption(portOption);
		options.addOption(usernameOption);
		options.addOption(passwordOption);
		options.addOption(queueOption);
		options.addOption(exchangeOption);
		options.addOption(debugOption);
		options.addOption(mappingEnabledOption);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch(ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("help", options);
			
			throw(e);
		}
		
		boolean isDebug = Boolean.valueOf(cmd.getOptionValue("d", Boolean.FALSE.toString()));
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("hostAndPort", cmd.getOptionValue("h") + ":" + cmd.getOptionValue("p"));
		params.put("username", cmd.getOptionValue("u"));
		params.put("password", cmd.getOptionValue("P"));
		params.put("queueName", cmd.getOptionValue("q"));
		params.put("exchangeName", cmd.getOptionValue("e", ""));
		params.put("mappingEnabled", cmd.getOptionValue("m"));
		
		HashMap<String, Object> config = new HashMap<>();
		config.put(ContextProperties.SUBMISSION_PARAMS, params);
		
		if(isDebug) {
			config.put(ContextProperties.TRACING_LEVEL, TraceLevel.TRACE);
		}
		
		VinesAdapterService svc = new VinesAdapterService();
		svc.run(Type.valueOf(cmd.getOptionValue("c", "DISTRIBUTED")), config);
		
		if(isDebug) {
			// launch a debug service to print raw messages to the console
			Topology rawMsgTopo = new Topology("VinesRawMsgDebug");
			rawMsgTopo.subscribe(VinesAdapterService.VINES_DEBUG_TOPIC, String.class).print();
			StreamsContextFactory.getStreamsContext(Type.DISTRIBUTED).submit(rawMsgTopo).get();
			
			// launch a debug service to print Observation tuples to the console
			Topology obsTopo = new Topology("VinesObservationDebug");
			SubscribeConnector.subscribe(obsTopo, VinesAdapterService.VINES_TOPIC).print();
			StreamsContextFactory.getStreamsContext(Type.DISTRIBUTED).submit(obsTopo).get();
			
			// launch a debug service to print errors to the console
			Topology errTopo = new Topology("VinesErrorDebug");
			errTopo.subscribe(VinesAdapterService.VINES_ERROR_TOPIC, String.class).print();
			StreamsContextFactory.getStreamsContext(Type.DISTRIBUTED).submit(errTopo).get();
		}
	}
}
