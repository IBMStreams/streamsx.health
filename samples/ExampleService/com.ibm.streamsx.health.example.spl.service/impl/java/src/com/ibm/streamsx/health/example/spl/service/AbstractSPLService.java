package com.ibm.streamsx.health.example.spl.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.TopologyElement;
import com.ibm.streamsx.topology.spl.SPL;
import com.ibm.xtq.xslt.runtime.RuntimeError;

public abstract class AbstractSPLService extends AbstractService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AbstractSPLService() {
		super();
	}

	@Override
	protected Topology createTopology() {
	
		Topology topo = new Topology(getName());
	
		try {
			String[] dependencies = getToolkitDependencies();
			addToolkitDependencies(topo, dependencies);
	
		} catch (IOException e) {
			throw new RuntimeError(e);
		}
	
		Map<String, Object> params = getParameters();
	
		SPL.invokeOperator(topo, getName(), getMainCompositeFQN(), null, null, params);
	
		return topo;
	}

	protected Map<String, Object> getParameters() {
		return null;
	}

	protected String[] getToolkitDependencies() {
		return new String[0];
	}

	protected void addToolkitDependencies(TopologyElement element, String[] dependencies) throws IOException {
		for (String toolkit : dependencies) {
			SPL.addToolkit(element, new File(toolkit));
		}
	}
	
	abstract String getName();
	
	
	abstract String getMainCompositeFQN();
	

}