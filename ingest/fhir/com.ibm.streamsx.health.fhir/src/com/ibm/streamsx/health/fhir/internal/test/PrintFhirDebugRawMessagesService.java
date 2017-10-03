package com.ibm.streamsx.health.fhir.internal.test;

import java.util.HashMap;
import java.util.Map;

import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.health.fhir.connector.FhirObxConnector;
import com.ibm.streamsx.health.fhir.service.AbstractFhirService;
import com.ibm.streamsx.health.fhir.service.IServiceConstants;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContextFactory;

/* 
 * This is an internal service for testing the debug connector
 */
public class PrintFhirDebugRawMessagesService extends AbstractFhirService {

	/**
	 * 
	 */ 
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		PrintFhirDebugRawMessagesService service = new PrintFhirDebugRawMessagesService();
		service.run();
	}

	@Override
	protected void run() {
		Topology topology = new Topology("FhirDebugRAwMessages");

		TStream<String> rawMessages = FhirObxConnector.subscribeDebug(topology, IServiceConstants.FHIR_OBX_DEBUG_TOPIC);
		rawMessages.print();

		try {
			Map<String, Object> subProperties = new HashMap<>();

			String vmArgs = getVmArgs();

			if (vmArgs != null && !vmArgs.isEmpty()) {
				// Add addition VM Arguments as specified in properties file
				subProperties.put(ContextProperties.VMARGS, vmArgs);
			}

			if (isDebug())
				subProperties.put(ContextProperties.TRACING_LEVEL, TraceLevel.DEBUG);

			StreamsContextFactory.getStreamsContext("DISTRIBUTED").submit(topology, subProperties);
		} catch (Exception e) {
			TRACE.error("Unable to submit topology", e);
		}
	}

}
