package com.ibm.streamsx.health.analyze.nlp.services;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.ibm.streamsx.health.microservices.AbstractSPLService;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.spl.SPL;

public class AnnotateDiseaseService extends AbstractSPLService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		AnnotateDiseaseService service = new AnnotateDiseaseService();
		service.run();
	}

	@Override
	protected String getMainCompositeFQN() {
		return "com.ibm.streamsx.health.analyze.nlp.services::AnnotateDiseaseService";
	}

	@Override
	protected void addDependencies(Topology topology) {
		
		String pearFile = getProperties().getProperty(IServiceConstants.KEY_PEARFILE);
		topology.addFileDependency(pearFile, "etc");
		
		super.addDependencies(topology);
		
		try {
			SPL.addToolkit(topology, new File("../../../ingest/common/com.ibm.streamsx.health.ingest"));
			SPL.addToolkit(topology, new File("../../../ext/toolkits/com.ibm.streamsx.health.nlp"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected Map<String, Object> getParameters() {
		Map<String, Object> params =  super.getParameters();
		String pearFile = getProperties().getProperty(IServiceConstants.KEY_PEARFILE);
		
		// HACK to get around issues with nlp operator not handling relative path correctly
		File file = new File(pearFile);
		if (!file.isAbsolute())
			pearFile = "../" + pearFile;

		params.put("pearFile", pearFile);
		
		return params;
	}
}
