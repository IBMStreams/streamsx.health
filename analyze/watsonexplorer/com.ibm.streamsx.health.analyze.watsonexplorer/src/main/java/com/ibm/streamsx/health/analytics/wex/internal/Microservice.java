package com.ibm.streamsx.health.analytics.wex.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;

public abstract class Microservice implements Serializable {
	private static final long serialVersionUID = 1L;
	private Topology topo;
	private String serviceName;
	private Type contextType = Type.DISTRIBUTED; // default
	private Map<String, Object> submissionTimeParams;
	private Map<String, Object> config;
	private String subscriptionTopic;
	
	public abstract String getPublishedTopic();
	public abstract void build() throws Exception;
		
	public Microservice(String serviceName) {
		this.serviceName = serviceName;
		topo = new Topology(serviceName);
		config = new HashMap<String, Object>();
		submissionTimeParams = new HashMap<String, Object>();
		config.put(ContextProperties.SUBMISSION_PARAMS, submissionTimeParams);
	}
	
	public void setContextType(Type contextType) {
		this.contextType = contextType;
	}
	
	public void addSubmissionTimeParam(String name, Object value) {
		submissionTimeParams.put(name, value);
	}
	
	public String getSubscriptionTopic() {
		return subscriptionTopic;
	}
	
	public void setTraceLevel(TraceLevel traceLevel) {
		config.put(ContextProperties.TRACING_LEVEL, traceLevel);
	}
	
	public Object buildAndRun() throws Exception {
		build();
		return StreamsContextFactory.getStreamsContext(this.contextType).submit(getTopology(), config).get();
	}
	
	public void setSubscriptionTopic(String subscriptionTopic) {
		this.subscriptionTopic = subscriptionTopic;
	}
	
	public Topology getTopology() {
		return this.topo;
	}
	
	public String getServiceName() {
		return this.serviceName;
	}
}