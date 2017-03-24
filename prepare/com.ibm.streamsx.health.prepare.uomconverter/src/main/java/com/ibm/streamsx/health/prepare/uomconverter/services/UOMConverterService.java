package com.ibm.streamsx.health.prepare.uomconverter.services;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.health.ingest.types.connector.PublishConnector;
import com.ibm.streamsx.health.ingest.types.connector.SubscribeConnector;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.prepare.uomconverter.ConversionResult;
import com.ibm.streamsx.health.prepare.uomconverter.ConverterFactory;
import com.ibm.streamsx.health.prepare.uomconverter.converters.BPMToBPSConverter;
import com.ibm.streamsx.health.prepare.uomconverter.converters.BPSToBPMConverter;
import com.ibm.streamsx.health.prepare.uomconverter.converters.CelciusToFahrenheitConverter;
import com.ibm.streamsx.health.prepare.uomconverter.converters.FahrenheitToCelciusConverter;
import com.ibm.streamsx.health.prepare.uomconverter.converters.VoltageConverter;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.function.Supplier;
import com.ibm.streamsx.topology.function.UnaryOperator;

import systems.uom.common.USCustomary;
import systems.uom.quantity.Information;
import tec.units.ri.spi.DefaultServiceProvider;
import tec.uom.lib.common.function.Parser;
import tec.uom.se.unit.Units;

public class UOMConverterService {

	public static final String UOM_TOPIC = "prepare-uom-converter";
	
	private Topology topo;
	private String subscribeTopic;
	private Supplier<String> uomMapFile;
	private Map<String, Object> config;
	private Map<String, Object> submissionParams;
	
	public UOMConverterService(String subscribeTopic) {
		this.subscribeTopic = subscribeTopic;
		topo = new Topology("UOMConverterService");
		topo.addClassDependency(Unit.class);
		topo.addClassDependency(Units.class);
		topo.addClassDependency(Parser.class);
		topo.addClassDependency(USCustomary.class);
		topo.addClassDependency(Information.class);
		topo.addClassDependency(DefaultServiceProvider.class);
		
		uomMapFile = topo.createSubmissionParameter("uom.map.file", String.class);
		config = new HashMap<String, Object>();
		submissionParams = new HashMap<String, Object>();
		
		config.put(ContextProperties.SUBMISSION_PARAMS, submissionParams);
	}
	
	public void setSubmissionParameter(String name, Object value) {
		submissionParams.put(name, value);
	}
	
	public void build() {
		/*
		 * You *MUST* register all available converters here. Otherwise,
		 * you will end up NoConverterFoundExceptions. 
		 */
		ConverterFactory factory = new ConverterFactory();
		factory.registerConverterClass(VoltageConverter.class);
		factory.registerConverterClass(CelciusToFahrenheitConverter.class);
		factory.registerConverterClass(FahrenheitToCelciusConverter.class);
		factory.registerConverterClass(BPSToBPMConverter.class);
		factory.registerConverterClass(BPMToBPSConverter.class);
		
		/*
		 * Build topology
		 */
		TStream<Observation> obsStream = SubscribeConnector.subscribe(topo, this.subscribeTopic);
		TStream<Observation> updateObsStream = obsStream.modify(new UnitConverter(factory, uomMapFile));
		
		PublishConnector.publishObservation(updateObsStream, getPublishedTopic());
	}
	
	public void run(Type contextType) throws Exception {
		build();
		StreamsContextFactory.getStreamsContext(contextType).submit(topo, config).get();
	}
	
	public String getPublishedTopic() {
		return UOM_TOPIC;
	}
	
	/**
	 * This class is responsible for converting the value in the incoming 
	 * Observation tuple from one unit to another. It takes a map file as
	 * an input and sends it to the ConverterFactory in order to generate
	 * the necessary converters.  
	 * 
	 * 
	 * @author streamsadmin
	 *
	 */
	private static class UnitConverter implements UnaryOperator<Observation> {
		private static final long serialVersionUID = 1L;
		
		private Supplier<String> uomMapFile; 
		private ConverterFactory factory;
		
		public UnitConverter(ConverterFactory factory, Supplier<String> uomMapFile) {
			this.factory = factory;
			this.uomMapFile = uomMapFile;
		}
		
		public Object readResolve() throws ObjectStreamException {
			try {
				factory.createConvertersFromMapFile(uomMapFile.get());
			} catch (Exception e) {
				e.printStackTrace();
				ObjectStreamException ose = new ObjectStreamException() {
					private static final long serialVersionUID = 1L;
				};
				ose.addSuppressed(e);
				throw ose;
			}
			return this;
		}
		
		@Override
		public Observation apply(Observation obs) {
			String uom = obs.getReading().getUom();
			
			if(factory.hasConverter(uom)) {
				ConversionResult result = factory.convert(obs.getReading().getValue(), uom);
				obs.getReading().setUom(result.getOutputUOM());
				obs.getReading().setValue(result.getConvertedValue());
			}
			
			return obs;
		}
	}	
	
	public static void main(String[] args) throws Exception {
		Option mapFileOption = Option.builder("m")
								.longOpt("map-file")
								.hasArg()
								.argName("map file path")
								.required()
								.build();
		
		Option subscriptionOption = Option.builder("s")
										.longOpt("subscription-topic")
										.hasArg()
										.argName("subscription topic")
										.required()
										.build();
		
		Option debugOption = Option.builder("d")
								.longOpt("debug")
								.hasArg()
								.argName("isDebugEnabled")
								.required(false)
								.type(Boolean.class)
								.build();
		
		Options options = new Options();
		options.addOption(mapFileOption);
		options.addOption(debugOption);
		options.addOption(subscriptionOption);
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("help", options);
			
			throw(e);
		}
		
		boolean isDebug = cmd.getOptionValue("d", "false").equals("true");
		
		UOMConverterService svc = new UOMConverterService(cmd.getOptionValue("s"));
		svc.setSubmissionParameter("uom.map.file", cmd.getOptionValue("m"));
		
		if(isDebug) {
			svc.config.put(ContextProperties.TRACING_LEVEL, TraceLevel.TRACE);
		}
		svc.run(Type.DISTRIBUTED);
		
		if(isDebug) {
			Topology t = new Topology("UOMConverterPrintService");
			SubscribeConnector.subscribe(t, UOM_TOPIC).print();
			StreamsContextFactory.getStreamsContext(Type.DISTRIBUTED).submit(t).get();
		}		
	}
}
