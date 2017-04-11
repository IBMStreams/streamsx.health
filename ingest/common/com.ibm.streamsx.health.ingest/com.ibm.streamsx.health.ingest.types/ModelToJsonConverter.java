package com.ibm.streamsx.health.hapi.internal;

import java.io.ObjectStreamException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.streamsx.topology.function.Function;

class ModelToJsonConverter<T> implements Function<T, String> {

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