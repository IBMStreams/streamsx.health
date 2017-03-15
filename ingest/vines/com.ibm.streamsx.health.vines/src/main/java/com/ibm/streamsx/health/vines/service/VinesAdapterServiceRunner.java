package com.ibm.streamsx.health.vines.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.health.ingest.types.connector.SubscribeConnector;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;

public class VinesAdapterServiceRunner {

	private static final String SUBMIT_PARAMS = "submitParams";
	private static final String IS_DEBUG_ENABLED = "isDebugEnabled";
	
	public static void main(String[] args) throws Exception {
		Map<String, Object> config = new HashMap<>();
		Map<String, Object> cmdLineOptions;
		
		try {
			cmdLineOptions = getCmdLineConfigs(args);
		} catch(ParseException e) {
			System.out.println(e.getMessage());
			return;
		}

		boolean isDebug = (boolean)cmdLineOptions.get(IS_DEBUG_ENABLED);
		
		@SuppressWarnings("unchecked")
		Map<String, Object> submitParams = (HashMap<String, Object>)cmdLineOptions.get(SUBMIT_PARAMS);
		config.put(ContextProperties.SUBMISSION_PARAMS, submitParams);
		
		if(isDebug) {
			config.put(ContextProperties.TRACING_LEVEL, TraceLevel.TRACE);
		}
		
		new VinesAdapterService().run(Type.DISTRIBUTED, config);
		
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

	@SuppressWarnings("static-access")
	private static HashMap<String, Object> getCmdLineConfigs(String[] args) throws ParseException {
		Options options = new Options();
		Option hostAndPort = OptionBuilder.withArgName("<host:port>")
											.hasArg()
											.withLongOpt("hostAndPort")
											.withDescription("Specify the host:port of the RabbitMQ server")
											.isRequired()
											.create("h");

		Option username = OptionBuilder.withArgName("username")
										.hasArg()
										.withLongOpt("username")
										.withDescription("Specify the username to connect to the RabbitMQ server")
										.isRequired()
										.create("u");
		
		Option password = OptionBuilder.withArgName("password")
										.hasArg()
										.withLongOpt("password")
										.withDescription("Specify the password to connect to the RabbitMQ server")
										.isRequired()
										.create("p");
		
		Option queueName = OptionBuilder.withArgName("queueName")
										.hasArg()
										.withLongOpt("queueName")
										.withDescription("Specify the RabbitMQ queue name")
										.isRequired()
										.create("q");
		
		Option exchange = OptionBuilder.withArgName("exchangeName")
										.hasArg()
										.withLongOpt("exchangeName")
										.withDescription("Specify the RabbitMQ exchange name (optional)")
										.create("e");
		
		Option debug = OptionBuilder.withArgName("debug")
										.withLongOpt("debug")
										.withDescription("Enables tracing and launches a service to connect to the debug port")
										.create("d");
		
		Option help = OptionBuilder.withLongOpt("help").withDescription("Display help").create();
		
		options.addOption(hostAndPort);
		options.addOption(username);
		options.addOption(password);
		options.addOption(queueName);
		options.addOption(exchange);
		options.addOption(help);
		options.addOption(debug);
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch(ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("help", options);
			
			throw(e);
		}
			
		HashMap<String, Object> cmdLineOptions = new HashMap<>();
		
		HashMap<String, Object> rabbitMQParams = new HashMap<>();
		rabbitMQParams.put("hostAndPort", cmd.getOptionValue("h"));
		rabbitMQParams.put("username", cmd.getOptionValue("u"));
		rabbitMQParams.put("password", cmd.getOptionValue("p"));
		rabbitMQParams.put("queueName", cmd.getOptionValue("q"));
		rabbitMQParams.put("exchangeName", cmd.getOptionValue("e", ""));

		cmdLineOptions.put(SUBMIT_PARAMS, rabbitMQParams);
		cmdLineOptions.put(IS_DEBUG_ENABLED, cmd.hasOption("debug"));
		
		return cmdLineOptions;
	}
	
}
