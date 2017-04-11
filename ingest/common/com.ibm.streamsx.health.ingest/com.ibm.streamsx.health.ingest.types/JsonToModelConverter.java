package com.ibm.streamsx.health.hapi.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.streams.operator.Tuple;
import com.ibm.streamsx.topology.function.Function;

public class JsonToModelConverter<T> implements Function<Tuple, T>{

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
	public T apply(Tuple v) {
		return gson.fromJson(v.getString("jsonString"), clazz);
	}

}
