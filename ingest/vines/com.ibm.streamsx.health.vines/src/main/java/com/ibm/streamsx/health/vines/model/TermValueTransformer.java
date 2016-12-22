package com.ibm.streamsx.health.vines.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TermValueTransformer implements JsonDeserializer<ITermValue>, JsonSerializer<ITermValue> {

	@Override
	public ITermValue deserialize(JsonElement json, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		ITermValue termValue = null;

		if(json instanceof JsonPrimitive) {
			termValue = new TermValueString(json.getAsString());
		} else if(json instanceof JsonObject) {
			termValue = context.deserialize(json, TermValueMap.class);
		}
		
		return termValue;
	}

	@Override
	public JsonElement serialize(ITermValue value, Type type, JsonSerializationContext context) {
		JsonElement json = null;
		if(value instanceof TermValueString) {
			json = new JsonPrimitive(((TermValueString)value).getValue());
		} else if(value instanceof TermValueMap) {
			json = context.serialize(value, type);
		}
		
		return json;
	}

}
