/* begin_generated_IBM_copyright_prolog                                       */
/*                                                                            */
/* This is an automatically generated copyright prolog.                       */
/* After initializing,  DO NOT MODIFY OR MOVE                                 */
/******************************************************************************/
/* Copyright (C) 2017 International Business Machines Corporation             */
/* All Rights Reserved                                                        */
/******************************************************************************/
/* end_generated_IBM_copyright_prolog                                         */

package com.ibm.streamsx.health.microservices.json;

import java.io.ObjectStreamException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.streamsx.topology.function.Function;

public class JsonToModelConverter<T> implements Function<String, T>{

	private static final long serialVersionUID = 1L;

	private transient Gson gson;
	
	private Class<T> clazz;
	
	public JsonToModelConverter(Class<T> clazz) {
		initGson();
		this.clazz = clazz;
	}
	
	private void initGson() {
		GsonBuilder builder = new GsonBuilder();
		gson = builder.create();
	}

	@Override
	public T apply(String v) {
		return gson.fromJson(v, clazz);
	}

	
	public Object readResolve() throws ObjectStreamException {
		initGson();
		return this;
	}
}
