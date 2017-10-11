//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.example.spl.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExampleSPLHealthService extends AbstractSPLService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		ExampleSPLHealthService service = new ExampleSPLHealthService();
		service.run();
	}

	protected Map<String, Object> getParameters() {
		String topic = getTopic();
		if (topic == null)
			throw new RuntimeException("Topic cannot be found in service.properties");

		Map<String, Object> params = new HashMap<>();
		params.put("subTopic", topic);
		return params;
	}

	private String getTopic() {
		return getProperties().getProperty(IServiceConstants.KEY_TOPIC, null);
	}

	protected String[] getToolkitDependencies() {

		String install = getStreamsInstall();

		ArrayList<String> dependencies = new ArrayList<>();

		dependencies.add("./");
		dependencies.add("../../../ingest/common/com.ibm.streamsx.health.ingest");
		dependencies.add(install + "/toolkits/com.ibm.streamsx.json");
		dependencies.add(install + "/toolkits/com.ibm.streamsx.topology");

		return (String[]) dependencies.toArray(new String[0]);
	}

	@Override
	String getName() {
		return "ExampleSPLHealthServiceWrapper";
	}

	@Override
	String getMainCompositeFQN() {
		return "com.ibm.streamsx.health.example.spl.service::ExampleSPLService";
	}

}
