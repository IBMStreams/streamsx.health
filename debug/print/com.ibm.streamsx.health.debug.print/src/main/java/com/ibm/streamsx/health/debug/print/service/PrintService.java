package com.ibm.streamsx.health.debug.print.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.ibm.streamsx.health.ingest.types.connector.JsonSubscriber;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;

public class PrintService {
	public static final String SERVICE_NAME = "PrintService";
	public static final String TOPIC = "debug-print";

	private Topology topo;
	private String subscribeTopic;

	public PrintService(String subscribeTopic) {
		topo = new Topology(SERVICE_NAME);
		this.subscribeTopic = subscribeTopic;
	}

	public void build() {
		JsonSubscriber.subscribe(topo, subscribeTopic).print();
	}

	public void run(Type contextType, Map<String, Object> config) throws Exception {
		build();
		StreamsContextFactory.getStreamsContext(contextType).submit(topo, config).get();
	}
	
	public static void main(String[] args) throws Exception {
		Option subscribeTopicOption = Option.builder("s")
											.longOpt("subscribe-topic")
											.hasArg()
											.argName("subscribe topic")
											.required()
											.build();
		
		Option contextTypeOption = Option.builder("c")
										 .longOpt("context-type")
										 .hasArg()
										 .argName("context type")
										 .required(false)
										 .build();
		
		Options options = new Options();
		options.addOption(subscribeTopicOption);
		options.addOption(contextTypeOption);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("help", options);
			
			throw(e);
		}
		
		Type contextType = Type.valueOf(cmd.getOptionValue("c", Type.DISTRIBUTED.name()));
		String subscribeTopic = cmd.getOptionValue("s");
		
		PrintService svc = new PrintService(subscribeTopic);
		svc.run(contextType, new HashMap<String, Object>());
	}

}
