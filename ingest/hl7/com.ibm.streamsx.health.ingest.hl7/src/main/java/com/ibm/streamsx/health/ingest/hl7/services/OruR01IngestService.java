package com.ibm.streamsx.health.ingest.hl7.services;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.health.ingest.hl7.internal.HapiMessageSupplier;
import com.ibm.streamsx.health.ingest.hl7.mapper.AbstractPlatformCodeMapper;
import com.ibm.streamsx.health.ingest.hl7.parser.AbstractOruR01ToObservationParser;
import com.ibm.streamsx.health.ingest.hl7.parser.ParserResult;
import com.ibm.streamsx.health.ingest.types.connector.IdentityMapper;
import com.ibm.streamsx.health.ingest.types.connector.PublishConnector;
import com.ibm.streamsx.health.ingest.types.connector.SubscribeConnector;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.function.Function;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

public class OruR01IngestService extends AbstractHL7Service {
	private static final long serialVersionUID = 1L;

	public static final String DEBUG_TOPIC = "oru-r01-debug-topic";
	private static final String SERVICE_NAME = "OruR01IngestService";
	
	public static final String ERROR_TOPIC = "oru-r01-error-topic";
	public static final String TOPIC = "oru-r01-topic";

	
	private AbstractOruR01ToObservationParser parser;
	private AbstractPlatformCodeMapper mapper;

	public OruR01IngestService(String hl7ToolkitPath, AbstractOruR01ToObservationParser parser,
			AbstractPlatformCodeMapper mapper, String topic, String errorTopic, int port) {
		super(SERVICE_NAME, hl7ToolkitPath, topic, errorTopic, port);
		this.parser = parser;
		this.mapper = mapper;
	}

	@Override
	public void build() {
		Topology topo = getTopology();

		TStream<Message> msgStream = topo.endlessSource(new HapiMessageSupplier(getPort()));

		TStream<ParserResult> mapperResult = msgStream.transform(parser);

		if (mapper != null)
			mapperResult = mapperResult.modify(mapper);

		// observation stream
		TStream<Observation> obsStream = mapperResult.multiTransform(new ObservationFunction());
		PublishConnector.mapAndPublish(obsStream, new IdentityMapper(), getTopic());

		// error stream
		TStream<String> errStream = mapperResult.transform(new ErrorFunction());
		errStream.publish(getErrorTopic());

		msgStream.transform(new MessageToString()).publish(DEBUG_TOPIC);
	}

	private static class MessageToString implements Function<Message, String> {
		private static final long serialVersionUID = 1L;

		@Override
		public String apply(Message msg) {
			try {
				return msg.encode();
			} catch (HL7Exception e) {
				e.printStackTrace();
				return "Unable to call encode() on message!";
			}
		}
	}

	private static class ErrorFunction implements Function<ParserResult, String> {
		private static final long serialVersionUID = 1L;

		private static Gson gson = new Gson();

		@Override
		public String apply(ParserResult result) {
			if (result.getErrorMessages().size() > 0) {
				JsonObject jsonObj = new JsonObject();
				jsonObj.add("errorMessages", gson.toJsonTree(result.getErrorMessages()));
				try {
					jsonObj.add("rawMessage", gson.toJsonTree(result.getHl7Message().encode()));
				} catch (HL7Exception e) {
					jsonObj.add("rawMessage", gson.toJsonTree("internal error parsing HL7 message"));
					e.printStackTrace();
				}

				return gson.toJson(jsonObj);
			}

			return null;
		}

	}

	private static class ObservationFunction implements Function<ParserResult, Iterable<Observation>> {
		private static final long serialVersionUID = 1L;

		@Override
		public Iterable<Observation> apply(ParserResult result) {
			return result.getObservations();
		}

	}

	public static void main(String[] args) {

		try {
			OruR01IngestService service = new OruR01IngestService("./", new OruR01ToObservationParser(),
					new DefaultPlatformCodeMapper(), TOPIC, ERROR_TOPIC, 8080);

			boolean isDebug = true;
			Type contextType = Type.DISTRIBUTED;

			Map<String, Object> config = new HashMap<String, Object>();
			if(isDebug) {
				config.put(ContextProperties.TRACING_LEVEL, TraceLevel.TRACE);
			}
			
			service.run(contextType, config);
			
			if(isDebug) {
				ErrorPrintService errorSvc = new ErrorPrintService(ERROR_TOPIC);
				errorSvc.run(contextType);
		
				ErrorPrintService debugSvc = new ErrorPrintService(OruR01IngestService.DEBUG_TOPIC);
				debugSvc.run(contextType);
				
				PrintService printSvc = new PrintService(TOPIC);
				printSvc.run(contextType);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static class ErrorPrintService {

		private Topology topo;

		public ErrorPrintService(String subscriptionTopic) {
			topo = new Topology("ErrorPrintService");
			topo.subscribe(subscriptionTopic, String.class).print();
		}

		public void run(Type contextType) throws Exception {
			StreamsContextFactory.getStreamsContext(contextType).submit(topo).get();
		}
	}

	private static class PrintService {

		private Topology topo;

		public PrintService(String subscriptionTopic) {
			topo = new Topology("PrintService");

			SubscribeConnector.subscribe(topo, subscriptionTopic).print();
		}

		public void run(Type contextType) throws Exception {
			StreamsContextFactory.getStreamsContext(contextType).submit(topo).get();
		}
	}
}
