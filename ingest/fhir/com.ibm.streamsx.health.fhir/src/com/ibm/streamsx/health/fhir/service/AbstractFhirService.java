//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.health.fhir.model.ObxQueryParams;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContextFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public abstract class AbstractFhirService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient IGenericClient fhirClient;
	private String serverBase;
	private Properties properties;

	protected static Logger TRACE = Logger.getLogger(AbstractFhirService.class);
	
	public AbstractFhirService() {
		
		initializeService();
	}

	protected void initializeService() {
		// Properties is serializable.  This code is called twice.
		// At the first time, when the Java application is run, it is called locally to generate
		// the SPL file.  At this time, the properties file should be accessible
		// and we read the data into a properties object.
		// At the second time, the Java object is being deserialized.  At this time, the properties
		// object is sent to the remote host as part of the seraizlied object.  We do not need
		// to read the file again and the properties attributes will still be available.
		if (properties == null)
			properties = readProperties();
		
		String url = properties.getProperty(IServiceConstants.KEY_BASEURL);
		
		if (url == null || url.isEmpty())
			throw new RuntimeException("baseurl is not specified in service.properties");
		
		this.serverBase = url;
	}
	
	protected  Properties readProperties()
	{
		try {
			FileInputStream iStream = new FileInputStream("service.properties");
			Properties properties = new Properties();
			properties.load(iStream);
			iStream.close();
			
			return properties;
			
		} catch (FileNotFoundException e) {
			TRACE.error("Unable to read patientIds.properties");
		} catch (IOException e) {
			TRACE.error("Unable to read patientIds.properties");
		}
		return null;
	}
	
	public Properties getProperties() {
		return properties;
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

		if (jarFiles != null) {
			for (File file : jarFiles) {
				topology.addJarDependency(file.getAbsolutePath());
			}
		}
		
		topology.addClassDependency(ObxQueryParams.class);
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
	
	protected boolean isDebug()
	{
		String property = getProperties().getProperty(IServiceConstants.KEY_DEBUG);
		if (property!=null && !property.isEmpty())
		{
			return Boolean.valueOf(property);
		}
		return false;
	}
	
	protected String getStreamsContext() {
		String property = getProperties().getProperty(IServiceConstants.KEY_STREAMS_CONTEXT);
		if (property !=null && !property.isEmpty())
		{
			return property.toUpperCase();
		}
		return "DISTRIBUTED";
	}
	
	protected String getVmArgs()
	{
		return getProperties().getProperty(IServiceConstants.KEY_VMARGS);
	}

	protected void submit(Topology topology) {
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

	protected void run() {
		
		Topology topo = createTopology();
		addDependencies(topo);
		submit(topo);
		
	}
	
	protected abstract Topology createTopology();

	
}
