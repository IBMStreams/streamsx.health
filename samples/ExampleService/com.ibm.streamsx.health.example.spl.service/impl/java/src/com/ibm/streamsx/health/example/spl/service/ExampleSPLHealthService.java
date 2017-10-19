//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.example.spl.service;

import java.util.ArrayList;

public class ExampleSPLHealthService extends AbstractSPLService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		ExampleSPLHealthService service = new ExampleSPLHealthService();
		service.run();
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
	String getMainCompositeFQN() {
		return "com.ibm.streamsx.health.example.spl.service::ExampleSPLService";
	}

}
