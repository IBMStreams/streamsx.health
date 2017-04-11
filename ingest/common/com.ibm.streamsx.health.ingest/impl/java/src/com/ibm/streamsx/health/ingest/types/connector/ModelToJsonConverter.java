/* begin_generated_IBM_copyright_prolog                                       */
/*                                                                            */
/* This is an automatically generated copyright prolog.                       */
/* After initializing,  DO NOT MODIFY OR MOVE                                 */
/******************************************************************************/
/* Copyright (C) 2017 International Business Machines Corporation             */
/* All Rights Reserved                                                        */
/******************************************************************************/
/* end_generated_IBM_copyright_prolog                                         */

package com.ibm.streamsx.health.ingest.types.connector;

import java.io.ObjectStreamException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.streamsx.topology.function.Function;

public class ModelToJsonConverter<T> implements Function<T, String> {

	private static final long serialVersionUID = 1L;

	private transient Gson gson;

	public ModelToJsonConverter() {
		initGson();
	}

	private void initGson() {
		GsonBuilder builder = new GsonBuilder();
		gson = builder.create();
	}

	@Override
	public String apply(T v) {
		return gson.toJson(v);
	}

	public Object readResolve() throws ObjectStreamException {
		initGson();
		return this;
	}
}