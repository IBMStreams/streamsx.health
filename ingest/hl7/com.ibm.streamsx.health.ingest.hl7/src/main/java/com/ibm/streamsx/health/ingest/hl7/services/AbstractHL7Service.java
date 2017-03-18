//*******************************************************************************
//* Copyright (C) 2016 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************
package com.ibm.streamsx.health.ingest.hl7.services;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;

abstract public class AbstractHL7Service implements Serializable{

	private static final long serialVersionUID = 1L;

	protected static Logger TRACE = Logger.getLogger(AbstractHL7Service.class);
	
	protected int port;
	protected String topic;
	protected String errorTopic;
	private Topology topology;
	protected String hl7ToolkitPath;
	
	public AbstractHL7Service(String serviceName, String hl7ToolkitPath, String topic, String errorTopic, int port) {
		this.topic = topic;
		this.errorTopic = errorTopic;
		this.port = port;
		this.hl7ToolkitPath = hl7ToolkitPath;
		
		topology = new Topology(serviceName);
		addDependencies();
	}

	public Topology getTopology() {
		return this.topology;
	}
	
	protected void addDependencies() {
		addDependency(hl7ToolkitPath + "release");
		addDependency(hl7ToolkitPath + "release/opt/lib");
	}
	
	private void addDependency(String dirPath) {
		File f = new File(dirPath);
		File[] jars = f.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		for(File jar : jars) {
			topology.addJarDependency(jar.getAbsolutePath());
		}
	}
	
	public int getPort(){
		return this.port;
	}
	
	public String getTopic() {
		return this.topic;
	}

	public String getErrorTopic() {
		return this.errorTopic;
	}
	
	public abstract void build();
	
	public void run(Type contextType, Map<String, Object> submissionParams) throws Exception {
		build();
		Map<String, Object> config = new HashMap<String, Object>();
		config.put(ContextProperties.SUBMISSION_PARAMS, submissionParams);
		
		StreamsContextFactory.getStreamsContext(contextType).submit(topology, config).get();
	}
}
