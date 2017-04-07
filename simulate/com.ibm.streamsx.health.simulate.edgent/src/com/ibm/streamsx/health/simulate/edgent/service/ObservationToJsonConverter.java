package com.ibm.streamsx.health.simulate.edgent.service;

import java.io.ObjectStreamException;

import org.apache.edgent.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.streamsx.health.ingest.types.model.Observation;

public class ObservationToJsonConverter implements Function<Observation, JsonObject> {

	private static final long serialVersionUID = 1L;

	private transient Gson gson;

	public ObservationToJsonConverter() {
		initGson();
	}

	private void initGson() {
		GsonBuilder builder = new GsonBuilder();
		gson = builder.create();
	}

	@Override
	public JsonObject apply(Observation v) {		
		JsonElement element = gson.toJsonTree(v);
		return element.getAsJsonObject();
	}

	public Object readResolve() throws ObjectStreamException {
		initGson();
		return this;
	}

}
