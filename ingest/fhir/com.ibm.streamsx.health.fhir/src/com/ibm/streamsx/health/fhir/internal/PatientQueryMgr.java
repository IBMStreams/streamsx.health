//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class PatientQueryMgr implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Hashtable<String, Long> timestampTable = new Hashtable<String, Long>();
	
	
	/**
	 * This method returns the time range that we should query data for
	 * @param patientId
	 * @return start and end time for query
	 */
	public List<Long> getTimeRange(String patientId)
	{
		// get last timestamp
		Long lastTs = timestampTable.get(patientId);
		Long currentTs = System.currentTimeMillis();
		
		if (lastTs == null)
			lastTs = Long.valueOf(0);
		
		// store the current time as the latest timestamp
		timestampTable.put(patientId, currentTs);
		
		List<Long> range = new ArrayList<Long>();
		range.add(lastTs);
		range.add(currentTs);
		
		return range;
	}

}
