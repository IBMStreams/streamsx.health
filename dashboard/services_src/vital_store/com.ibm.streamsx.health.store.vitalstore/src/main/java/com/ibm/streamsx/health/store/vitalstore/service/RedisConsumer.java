package com.ibm.streamsx.health.store.vitalstore.service;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.resolver.ObservationTypeResolver;
import com.ibm.streamsx.topology.function.Consumer;
import com.ibm.streamsx.topology.function.FunctionContainer;
import com.ibm.streamsx.topology.function.FunctionContext;
import com.ibm.streamsx.topology.function.Initializable;
import com.ibm.streamsx.topology.function.Supplier;
import com.lambdaworks.redis.Range;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.sync.RedisCommands;

public class RedisConsumer implements Consumer<Observation>, Initializable {
	private static final long serialVersionUID = 1L;

	private static final String KEY_PREFIX = "com.ibm.streamsx.health:data:";
	private static final String APP_CONFIG_CONNECTION_PROPERTY = "connection";
	
	private transient RedisClient client;
	private transient RedisCommands<String, String> sync;
	private transient Gson gson;
	private Supplier<String> appConfigNameSupplier;
	private Supplier<Long> expireTimeSupplier;
	private Map<String, List<String>> dataTypes;
	
	private long expireTimeMilliseconds;

	private FunctionContext functionContext;
	
	public RedisConsumer(Supplier<String> appConfigNameSupplier, Supplier<Long> expireTimeSupplier) {
		this.appConfigNameSupplier = appConfigNameSupplier;
		this.expireTimeSupplier = expireTimeSupplier;
		dataTypes = new HashMap<String, List<String>>();
	}
	
	public Object readResolve() throws ObjectStreamException {
		gson = new Gson();
		this.expireTimeMilliseconds = this.expireTimeSupplier.get() * 1000l;
		
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
		String key = KEY_PREFIX + obs.getPatientId() + ":" + ObservationTypeResolver.getLabel(obs);
		JsonObject jsonValue = new JsonObject();
		jsonValue.addProperty("ts", obs.getReading().getTimestamp());
		jsonValue.addProperty("value", obs.getReading().getValue());

		try {
			// run commands atomically
			//  - remove old entries
			//  - add the new observation
			System.out.println("Deleting: key=" + key + ", from=" + 0 + ", to=" + (obs.getReading().getTimestamp() - this.expireTimeMilliseconds));
			RedisCommands<String, String> redis = getRedis();
			redis.zremrangebyscore(key, Range.create(0, obs.getReading().getTimestamp() - this.expireTimeMilliseconds));
			redis.zadd(key, obs.getReading().getTimestamp(), gson.toJson(jsonValue));
			
			updateAvailableDataTypes(obs);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private void updateAvailableDataTypes(Observation obs) throws Exception {
		String patientId = obs.getPatientId();
		String key = KEY_PREFIX + patientId + ":data_type_index";
		String dataType = ObservationTypeResolver.getLabel(obs);
		
		if(!dataTypes.containsKey(patientId)) {
			dataTypes.put(patientId, new ArrayList<>());
		}
		
		if(!dataTypes.get(patientId).contains(dataType)) {
			dataTypes.get(patientId).add(dataType);
			getRedis().sadd(key, dataType);
		}
	}
}
