//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.notify.email;

import java.util.ArrayList;

public class SendMailService extends AbstractSPLService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SendMailService service = new SendMailService();
		service.run();
	}


	protected String[] getToolkitDependencies() {

//		String install = getStreamsInstall();

		ArrayList<String> dependencies = new ArrayList<>();

		dependencies.add("./");

		return (String[]) dependencies.toArray(new String[0]);
	}

	@Override
	String getMainCompositeFQN() {
		return "com.ibm.streamsx.health.notify.email.service::EmailService";
	}

}
