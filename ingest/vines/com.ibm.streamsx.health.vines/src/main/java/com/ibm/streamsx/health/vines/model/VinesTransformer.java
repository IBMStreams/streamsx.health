/*******************************************************************************
 * Copyright (C) 2017 International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/
package com.ibm.streamsx.health.vines.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class VinesTransformer implements JsonDeserializer<Vines> {

	@Override
	public Vines deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		Vines vines = new Vines();
		
		if(json instanceof JsonObject) {
			JsonObject jsonObj = (JsonObject)json;
			if(jsonObj.has("Data")) {
				// no special handling required, just continue with deserializing
				Data data = context.deserialize(jsonObj.get("Data"), Data.class);
				RootId id = context.deserialize(jsonObj.get("_id"), RootId.class);
				vines.setData(data);
				vines.set_id(id);
			} else {
				if(jsonObj.has("Body")) {
					// Model expects "Data" to be top-level object, 
					// but instead "Body" is at the top-level.
					// Create new JsonObject with "Data" as the top-level
					// object and set the "json" parameter as the value
					JsonObject dataJson = new JsonObject();
					dataJson.add("Data", json);
					vines = context.deserialize(dataJson, Vines.class);
				} else {
					throw new JsonParseException("JSON structure note recognized: " + json.toString());
				}
			}
		}
		
		return vines;
	}

}
