package com.ibm.streamsx.health.ingest.hl7.parser;

import com.ibm.streamsx.topology.function.Function;

import ca.uhn.hl7v2.model.Message;

public abstract class AbstractOruR01ToObservationParser implements Function<Message, ParserResult> {
	private static final long serialVersionUID = 1L;

}
