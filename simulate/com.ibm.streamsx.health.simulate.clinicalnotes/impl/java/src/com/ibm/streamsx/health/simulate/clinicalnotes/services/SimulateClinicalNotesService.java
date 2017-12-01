package com.ibm.streamsx.health.simulate.clinicalnotes.services;

import java.io.File;
import java.io.IOException;

import com.ibm.streamsx.health.microservices.AbstractSPLService;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.spl.SPL;

public class SimulateClinicalNotesService extends AbstractSPLService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		SimulateClinicalNotesService service = new SimulateClinicalNotesService();
		service.run();
	}

	@Override
	protected String getMainCompositeFQN() {
		return "com.ibm.streamsx.health.simulate.clinicalnotes.services::SimulateClinicalNotesService";
	}
	
	@Override
	protected void addDependencies(Topology topology) {
		super.addDependencies(topology);
		
		try {
			SPL.addToolkit(topology, new File("../../ingest/common/com.ibm.streamsx.health.ingest"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
