package com.ibm.streamsx.health.demo.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.spl.SPL;

public class UIWrapperService {

	private Topology topo;
	
	public UIWrapperService() throws Exception {
		topo = new Topology("UIService");
		SPL.addToolkit(topo, new File(System.getProperty("user.dir")));
		SPL.addToolkit(topo, new File(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streamsx.messaging"));
		SPL.addToolkit(topo, new File(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streamsx.json"));
		SPL.addToolkit(topo, new File(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streams.rulescompiler"));
		SPL.addToolkit(topo, new File("../../ingest/common/com.ibm.streamsx.health.ingest"));
		SPL.addToolkit(topo, new File(System.getProperty("user.dir") + "/.toolkits/com.ibm.streamsx.health.analyze.vital"));
		SPL.addToolkit(topo, new File(System.getProperty("user.dir") + "/.toolkits/com.ibm.streamsx.inet"));
	}
	
	public void build() {
		SPL.invokeOperator(topo, "UIService", "com.ibm.streamsx.health.demo.ui::UIService", null, null, new HashMap<String, Object>());
	}
	
	public void run(Type type, Map<String, Object> submissionParams) throws Exception {
		build();
		StreamsContextFactory.getStreamsContext(type).submit(topo).get();
	}

	
}
