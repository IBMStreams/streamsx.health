package com.ibm.streamsx.health.demo;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.ibm.streamsx.health.demo.service.PatientManipulatorService;
import com.ibm.streamsx.health.demo.service.UIWrapperService;
import com.ibm.streamsx.health.demo.service.VitalsRulesWrapperService;
import com.ibm.streamsx.health.simulate.beacon.services.HealthDataBeaconService;
import com.ibm.streamsx.topology.context.StreamsContext.Type;

public class PatientsMonitoringDemo {

	private static Type contextType = Type.DISTRIBUTED;
	
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		
		Option contextTypeOption = Option.builder("t")
									.required(false)
									.hasArg(true)
									.desc("Specify the Streams context type (BUNDLE, DISTRIBUTED, etc)")
									.longOpt("type")
									.build();

		Option helpOption = Option.builder("h")
								.longOpt("help")
								.desc("Display help")
								.build();
		
		options.addOption(contextTypeOption);
		options.addOption(helpOption);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmdLine = parser.parse(options, args);
		
		if(cmdLine.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("PatientsMonitoringDemo", options);
			return;
		} else if(cmdLine.hasOption("t")) {
			String type = cmdLine.getOptionValue("t");
			contextType = Type.valueOf(type.toUpperCase());
			
			if(contextType == null) {
				System.out.println("ERROR: Invalid contextType " + type);
			}
		}
		
		// UI service
		UIWrapperService uiService = new UIWrapperService();
		
		// DataIngest service
		Map<String, Object> beaconServiceParams = new HashMap<String, Object>();
		beaconServiceParams.put("num.patients", 50);
		HealthDataBeaconService beaconService = new HealthDataBeaconService("../../simulate/com.ibm.streamsx.health.simulate.beacon");
		
		// Manipulator service
		PatientManipulatorService manService = new PatientManipulatorService(beaconService.getPublishedTopic());

		// Vitals Rules
		VitalsRulesWrapperService vitalsService = new VitalsRulesWrapperService();
		Map<String, Object> vitalsParams = new HashMap<String, Object>();
		vitalsParams.put("ingestTopic", manService.getPublishedTopic());
		
		uiService.run(contextType, new HashMap<>());
		beaconService.run(contextType, beaconServiceParams);
		manService.run(contextType, new HashMap<>());
		vitalsService.run(contextType, vitalsParams);
	}

}
