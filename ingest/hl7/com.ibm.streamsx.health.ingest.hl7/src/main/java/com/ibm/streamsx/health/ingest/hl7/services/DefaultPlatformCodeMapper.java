package com.ibm.streamsx.health.ingest.hl7.services;

import com.ibm.streamsx.health.ingest.hl7.mapper.AbstractPlatformCodeMapper;
import com.ibm.streamsx.health.ingest.hl7.parser.ParserResult;

public class DefaultPlatformCodeMapper extends AbstractPlatformCodeMapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public ParserResult apply(ParserResult v) {
		return v;
	}

}
