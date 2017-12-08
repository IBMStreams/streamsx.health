package com.ibm.streamsx.health.store.locationstore.service;

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

public class RedisConsumer implements Consumer<Location>, Initializable {
	private static final long serialVersionUID = 1L;

	private static final String KEY_PREFIX = "com.ibm.streamsx.health:data:";
	private static final String APP_CONFIG_CONNECTION_PROPERTY = "connection";
	private static final String DATA_TYPE = "location";
	
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
	public void accept(Location location) {
		String key = KEY_PREFIX + location.getId() + ":" + DATA_TYPE;		

		try {
			// run commands atomically
			//  - remove old entries
			//  - add the new observation
			System.out.println("Deleting: key=" + key + ", from=" + 0 + ", to=" + (location.getTs() - this.expireTimeMilliseconds));
			RedisCommands<String, String> redis = getRedis();
			redis.zremrangebyscore(key, Range.create(0, location.getTs() - this.expireTimeMilliseconds));
			redis.zadd(key, location.getTs(), gson.toJson(location));
			
			updateAvailableDataTypes(location);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private void updateAvailableDataTypes(Location location) throws Exception {
		String patientId = location.getId();
		String key = KEY_PREFIX + patientId + ":data_type_index";
		String dataType = DATA_TYPE;
		
		if(!dataTypes.containsKey(patientId)) {
			dataTypes.put(patientId, new ArrayList<>());
		}
		
		if(!dataTypes.get(patientId).contains(dataType)) {
			dataTypes.get(patientId).add(dataType);
			getRedis().sadd(key, dataType);
		}
	}
}
