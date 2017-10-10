//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.analyze.rpeak.cep.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.spl.SPL;
import com.ibm.xtq.xslt.runtime.RuntimeError;

public class RPeakDetectCepService extends AbstractService{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	public static void main(String[] args) {
		RPeakDetectCepService service = new RPeakDetectCepService();
		service.run();
	}

	@Override
	protected Topology createTopology() {

		Topology topo = new Topology("RPeakDetectCepService");
		
		try {
			SPL.addToolkit(topo, new File("./"));
			SPL.addToolkit(topo, new File("../../../ingest/common/com.ibm.streamsx.health.ingest"));
		} catch (IOException e) {
			throw new RuntimeError(e);
		}
		
		String topic = getTopic();
		if (topic == null)
			throw new RuntimeException("Topic cannot be found in service.properties");
		
		String readingCode = getReadingCode();
		Double threshold = getPeakThreshold();
		
		Map<String, Object> params = new HashMap<>();
		params.put("subTopic", topic);
		params.put("readingCode", readingCode);
		params.put("peakThreshold", threshold);
		
		SPL.invokeOperator(topo, "RPeakCep", "com.ibm.streamsx.health.analyze.rpeak.cep.service::RPeakDetectCep", null, null, params);
		
		return topo;
	}
	
	private String getTopic() {
		return getProperties().getProperty(IServiceConstants.KEY_TOPIC, null);
	}
	
	private Double getPeakThreshold() {
		return Double.valueOf(getProperties().getProperty(IServiceConstants.KEY_PEAKTHRESHOLD, "0.8"));
	}

	private String getReadingCode() {
		return getProperties().getProperty(IServiceConstants.KEY_READINGCODE, "ECG");
	}
	
}
