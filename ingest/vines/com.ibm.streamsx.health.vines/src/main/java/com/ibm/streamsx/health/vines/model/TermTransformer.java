/*******************************************************************************
 * Copyright (C) 2017 International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/
package com.ibm.streamsx.health.vines.model;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class TermTransformer implements JsonDeserializer<ITerm> {

	@Override
	public ITerm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		ITerm term = null;
		if(json instanceof JsonObject) {
			term = context.deserialize(json, Term.class);
		} else if(json instanceof JsonArray) {
			term = context.deserialize(json, TermArray.class);
		}
		return term;
	}

}
