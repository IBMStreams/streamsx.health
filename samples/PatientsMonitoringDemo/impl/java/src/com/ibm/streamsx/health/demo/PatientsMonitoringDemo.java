package com.ibm.streamsx.health.demo;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.ibm.streamsx.health.demo.service.PatientManipulatorService;
import com.ibm.streamsx.health.demo.service.UIWrapperService;
import com.ibm.streamsx.health.demo.service.VitalsRulesWrapperService;
import com.ibm.streamsx.health.simulate.beacon.services.HealthDataBeaconService;
import com.ibm.streamsx.topology.context.StreamsContext.Type;

public class PatientsMonitoringDemo {

	private static Type contextType = Type.BUNDLE;

	public static void main(String[] args) throws Exception {

		String topics = "";
		int numPatients = 20;

		Options options = new Options();

		Option contextTypeOption = Option.builder("t").required(false).hasArg(true)
				.desc("Specify the Streams context type (BUNDLE, DISTRIBUTED, etc)").longOpt("type").build();

		Option topicOption = Option.builder("i").required(true).hasArg(true)
				.desc("Specify a list of topics the sample should also subscribe to.  Topics are specified as comma-separated list.")
				.longOpt("topic").build();

		Option numPatientsOption = Option.builder("n").required(false).hasArg(true)
				.desc("Specify number of patients to generate.").longOpt("num").build();

		Option helpOption = Option.builder("h").longOpt("help").desc("Display help").build();

		options.addOption(contextTypeOption);
		options.addOption(topicOption);
		options.addOption(numPatientsOption);
		options.addOption(helpOption);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmdLine = parser.parse(options, args);

		if (cmdLine.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("PatientsMonitoringDemo", options);
			return;
		} 
		
		if (cmdLine.hasOption("t")) {
			String type = cmdLine.getOptionValue("t");
			contextType = Type.valueOf(type.toUpperCase());

			if (contextType == null) {
				System.out.println("ERROR: Invalid contextType " + type);
			}
		} 
		
		if (cmdLine.hasOption("i")) {
			topics = cmdLine.getOptionValue("i");

			if (topics == null) {
				System.out.println("ERROR: Invalid topics specified. Topics cannot be null.");
				return;
			} 
			else if (topics.length() == 0) {
				System.out.println("ERROR: Invalid topics specified. Topics cannot be empty.");
				return;
			}
		} 
		
		if (cmdLine.hasOption("n")) {
			String numPatientsStr = cmdLine.getOptionValue("n");

			if (numPatientsStr != null) {
				try {
					numPatients = Integer.valueOf(numPatientsStr);

					if (numPatients < 0) {
						System.out.println("ERROR: Number of patients cannot be < 0.");
						return;
					}
				} catch (Exception e) {
					System.out.println("ERROR: Unable to convert number of patients to a number: " + numPatientsStr);
					return;
				}
			}
		}

		// UI service
		UIWrapperService uiService = new UIWrapperService();

		// DataIngest service
		if (numPatients > 0) {
			Map<String, Object> beaconServiceParams = new HashMap<String, Object>();
			beaconServiceParams.put("num.patients", numPatients);
			beaconServiceParams.put("patient.prefix", "patient-");
			HealthDataBeaconService beaconService = new HealthDataBeaconService(
					"../../simulate/com.ibm.streamsx.health.simulate.beacon");
			beaconService.run(contextType, beaconServiceParams);
		}

		// Manipulator service
		PatientManipulatorService manService = new PatientManipulatorService(topics.split(","));

		// Vitals Rules
		VitalsRulesWrapperService vitalsService = new VitalsRulesWrapperService();
		Map<String, Object> vitalsParams = new HashMap<String, Object>();
		vitalsParams.put("ingestTopic", manService.getPublishedTopic());
		uiService.run(contextType, new HashMap<>());
		manService.run(contextType, new HashMap<>());
		vitalsService.run(contextType, vitalsParams);
	}

}
