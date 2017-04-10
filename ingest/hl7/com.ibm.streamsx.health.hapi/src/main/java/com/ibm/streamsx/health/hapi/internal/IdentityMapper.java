package com.ibm.streamsx.health.hapi.internal;

import java.util.ArrayList;
import java.util.List;

import com.ibm.streamsx.health.ingest.types.model.ADTEvent;
import com.ibm.streamsx.topology.function.Function;

public class IdentityMapper<T>  implements Function<T, Iterable<ADTEvent>> {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -8266715709665301322L;
	

	public List<ADTEvent> map(T v) {
		List<ADTEvent> events = new ArrayList<ADTEvent>();
		events.add((ADTEvent) v);
		
		return events;
	}

	@Override
	public Iterable<ADTEvent> apply(T v) {
		return map(v);
	}

}