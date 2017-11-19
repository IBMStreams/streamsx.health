package com.ibm.streamsx.health.analyze.patientdiscovery.services;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.function.Consumer;
import com.ibm.streamsx.topology.function.FunctionContainer;
import com.ibm.streamsx.topology.function.FunctionContext;
import com.ibm.streamsx.topology.function.Initializable;
import com.ibm.streamsx.topology.function.Supplier;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.sync.RedisCommands;

public class RedisConsumer implements Consumer<Observation>, Initializable {
	private static final long serialVersionUID = 1L;

	private static final String KEY = "com.ibm.streamsx.health:patients";
	private static final String APP_CONFIG_CONNECTION_PROPERTY = "connection";
	
	private transient RedisClient client;
	private transient RedisCommands<String, String> sync;
	private transient Gson gson;
	private Map<String /* patientId */, String /* readingSource */> patientMap;
	private Supplier<String> appConfigNameSupplier;

	private FunctionContext functionContext;
	
	public RedisConsumer(Supplier<String> appConfigNameSupplier) {
		this.appConfigNameSupplier = appConfigNameSupplier;
		this.patientMap = new HashMap<String, String>();
	}
	
	public Object readResolve() throws ObjectStreamException {
		gson = new Gson();
		
		return this;
	}

	private RedisCommands<String, String> getRedis() throws Exception{
		if(sync == null) {
			FunctionContainer container = functionContext.getContainer();
			Map<String, String> appConfig = container.getApplicationConfiguration(appConfigNameSupplier.get());
			String connectionString = appConfig.get(APP_CONFIG_CONNECTION_PROPERTY);
			if(connectionString == null) {
				throw new Exception("No value set for 'connection' property in app config \"" + appConfigNameSupplier.get() + "\"");
			}

			client = RedisClient.create("redis://" + connectionString);
			sync = client.connect().sync();			
		}
		
		return sync;
	}
	
	@Override
	public void initialize(FunctionContext functionContext) throws Exception {
		this.functionContext = functionContext;
	}
	
	@Override
	public void accept(Observation obs) {
		String patientId = obs.getPatientId();
		
		JsonObject dataObj = new JsonObject();
		dataObj.addProperty("readingSource", obs.getReadingSource().getId());
		String dataObjStr = gson.toJson(dataObj);

		if(!patientMap.containsKey(patientId) || !patientMap.get(patientId).equals(dataObjStr)) {
			patientMap.put(patientId, dataObjStr);
			
			try {
				RedisCommands<String, String> redis = getRedis();
				redis.hmset(KEY, patientMap);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}	
		}
	}
}
