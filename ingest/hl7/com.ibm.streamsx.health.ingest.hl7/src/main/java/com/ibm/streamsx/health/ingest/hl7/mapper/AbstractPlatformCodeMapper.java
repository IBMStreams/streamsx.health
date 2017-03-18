package com.ibm.streamsx.health.ingest.hl7.mapper;

import com.ibm.streamsx.health.ingest.hl7.parser.ParserResult;
import com.ibm.streamsx.topology.function.UnaryOperator;

public abstract class AbstractPlatformCodeMapper implements UnaryOperator<ParserResult> {
	private static final long serialVersionUID = 1L;

}
