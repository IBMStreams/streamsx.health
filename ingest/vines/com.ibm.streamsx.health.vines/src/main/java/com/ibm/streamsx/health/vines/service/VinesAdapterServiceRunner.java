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

import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContext.Type;

public class VinesAdapterServiceRunner {

	public static void main(String[] args) throws Exception {
		Map<String, Object> config = new HashMap<>();
		try {
			Map<String, Object> submitParams = getCmdLineConfigs(args);
			config.put(ContextProperties.SUBMISSION_PARAMS, submitParams);
		} catch(ParseException e) {
			System.out.println(e.getMessage());
			return;
		}
		
		new VinesAdapterService().run(Type.DISTRIBUTED, config);
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
		Option help = OptionBuilder.withLongOpt("help").withDescription("Display help").create();
		
		options.addOption(hostAndPort);
		options.addOption(username);
		options.addOption(password);
		options.addOption(queueName);
		options.addOption(exchange);
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch(ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("help", options);
			
			throw(e);
		}
			
		HashMap<String, Object> rabbitMQParams = new HashMap<>();
		rabbitMQParams.put("hostAndPort", cmd.getOptionValue("h"));
		rabbitMQParams.put("username", cmd.getOptionValue("u"));
		rabbitMQParams.put("password", cmd.getOptionValue("p"));
		rabbitMQParams.put("queueName", cmd.getOptionValue("q"));
		rabbitMQParams.put("exchangeName", cmd.getOptionValue("e", ""));
				
		return rabbitMQParams;
	}

}
