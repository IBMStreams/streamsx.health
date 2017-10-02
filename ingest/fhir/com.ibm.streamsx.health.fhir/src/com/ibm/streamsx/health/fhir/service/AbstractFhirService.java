//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;

import org.apache.log4j.Logger;

import com.ibm.streamsx.topology.Topology;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;

public abstract class AbstractFhirService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient IGenericClient fhirClient;
	private String serverBase;

	protected static Logger TRACE = Logger.getLogger(AbstractFhirService.class);

	public AbstractFhirService(String serverBase) {
		this.serverBase = serverBase;
	}

	protected void addDependencies(Topology topology) {

		File dir = new File("opt/lib");
		
		// only include the required Jar files in classpath
		// exclude any IBM Streams jar files and topology jar files
		File[] jarFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("jar") && (name.indexOf("streams.") == -1)
						&& (name.indexOf("com.ibm.streamsx.topology") == -1);
			}
		});

		for (File file : jarFiles) {
			topology.addJarDependency(file.getAbsolutePath());
		}
	}

	public IGenericClient getFhirClient() {

		if (fhirClient == null) {
			fhirClient = createClient();
		}

		return fhirClient;
	}

	protected IGenericClient createClient() {
		FhirContext ctx = FhirContext.forDstu3();
		IGenericClient client = ctx.newRestfulGenericClient(serverBase);
		return client;
	}

	abstract void run();

}
