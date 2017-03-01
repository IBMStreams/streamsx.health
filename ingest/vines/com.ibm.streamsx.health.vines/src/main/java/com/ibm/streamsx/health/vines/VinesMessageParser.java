package com.ibm.streamsx.health.vines;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.streamsx.health.vines.model.ITerm;
import com.ibm.streamsx.health.vines.model.ITermValue;
import com.ibm.streamsx.health.vines.model.TermTransformer;
import com.ibm.streamsx.health.vines.model.TermValueTransformer;
import com.ibm.streamsx.health.vines.model.Vines;

public class VinesMessageParser {

	private static Gson createGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ITerm.class, new TermTransformer());
		builder.registerTypeAdapter(ITermValue.class, new TermValueTransformer());
		
		return builder.create();
	}
	
	public static Vines fromJson(String jsonString) {
		Gson gson = createGson();
		Vines vinesMsg = gson.fromJson(jsonString, Vines.class);
		vinesMsg.setRawMessage(jsonString);
		
		return vinesMsg;
	}
	
	public static String toJson(Vines vines) {
		Gson gson = createGson();
		return gson.toJson(vines);
	}
}
