package com.ibm.streamsx.health.control.patientcontrolplane.topology;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.spl.SPL;

public class PatientControlPlane {

	public static enum ServiceType {
		Adapter,
		Aggregator,
		App
	};
	
	public static void addPatientControlPlane(Topology topo, String serviceName, ServiceType serviceType, List<String> outputTopics) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("serviceName", serviceName);
		params.put("serviceType", serviceType.name().toLowerCase());
		
		if(outputTopics != null && !outputTopics.isEmpty())
			params.put("outputTopics", outputTopics.get(0));
				
		SPL.invokeOperator(topo, "PatientControlPlane", "com.ibm.streamsx.health.control.patientcontrolplane::PatientControlPlane", Collections.emptyList(), Collections.emptyList(), params);
	}

}


