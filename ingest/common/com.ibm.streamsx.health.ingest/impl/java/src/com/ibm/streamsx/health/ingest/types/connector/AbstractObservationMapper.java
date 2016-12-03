/* begin_generated_IBM_copyright_prolog                                       */
/*                                                                            */
/* This is an automatically generated copyright prolog.                       */
/* After initializing,  DO NOT MODIFY OR MOVE                                 */
/******************************************************************************/
/* Copyright (C) 2016 International Business Machines Corporation             */
/* All Rights Reserved                                                        */
/******************************************************************************/
/* end_generated_IBM_copyright_prolog                                         */
package com.ibm.streamsx.health.ingest.types.connector;

import java.util.List;

import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.function.Function;

public abstract class AbstractObservationMapper<T> implements Function<T, Iterable<Observation>> {

	private static final long serialVersionUID = 1L;

	@Override
	public Iterable<Observation> apply(T t) {
		return map(t);
	}

	/**
	 * This method should return a list of Observation objects
	 * that were created from the input message T.
	 * 
	 * @param t input message to map from
	 * @return a list of Observation objects
	 */
	public abstract List<Observation> map(T t);
}
