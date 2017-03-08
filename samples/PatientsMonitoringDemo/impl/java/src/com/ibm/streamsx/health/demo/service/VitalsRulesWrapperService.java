package com.ibm.streamsx.health.demo.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.spl.SPL;

public class VitalsRulesWrapperService {

	private Topology topo;
	
	public VitalsRulesWrapperService() throws Exception {
		topo = new Topology("VitalsRulesServices");
		SPL.addToolkit(topo, new File(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streams.rulescompiler"));
		SPL.addToolkit(topo, new File("../../ingest/common/com.ibm.streamsx.health.ingest"));
		SPL.addToolkit(topo, new File(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streamsx.json"));
		SPL.addToolkit(topo, new File(System.getProperty("user.dir") + "/.toolkits/com.ibm.streamsx.health.analyze.vital"));
	}
	
	public void build() {
		SPL.invokeOperator(topo, "VitalsRules", "com.ibm.streamsx.health.analyze.vital::VitalRangeCheckService", null, null, new HashMap<String, Object>());
	}
	
	public void run(Type type, Map<String, Object> submissionParams) throws Exception {
		build();
		
		Map<String, Object> config = new HashMap<String, Object>();
		config.put(ContextProperties.SUBMISSION_PARAMS, submissionParams);
		StreamsContextFactory.getStreamsContext(type).submit(topo, config).get();
	}
	
}
