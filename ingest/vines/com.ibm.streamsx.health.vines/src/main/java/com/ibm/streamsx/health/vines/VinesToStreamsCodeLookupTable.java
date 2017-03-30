/*******************************************************************************
 * Copyright (C) 2017 International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/
package com.ibm.streamsx.health.vines;

import java.io.InputStream;

import com.ibm.streamsx.health.ingest.mapper.AbstractLookupTable;

public class VinesToStreamsCodeLookupTable extends AbstractLookupTable {
	private static final long serialVersionUID = 1L;

	private static final int PLATFORM_CODE_COL = 1;

	public VinesToStreamsCodeLookupTable(InputStream inputStream) throws Exception {
		super();
		loadCSV(inputStream);
	}
	
	public String lookupPlatformCode(String vinesCode) {
		return lookup(vinesCode, PLATFORM_CODE_COL);
	}
}
