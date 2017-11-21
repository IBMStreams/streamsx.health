package com.ibm.streamsx.health.control.patientcontrolplane.operator.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ibm.streamsx.health.control.patientcontrolplane.operator.Constants;
import com.ibm.streamsx.health.control.patientcontrolplane.operator.ServiceInfo;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.sync.RedisCommands;
import com.lambdaworks.redis.pubsub.RedisPubSubListener;
import com.lambdaworks.redis.pubsub.api.sync.RedisPubSubCommands;

public class RedisServerAdapter implements ExternalServerAdapter {

	public static interface RedisBackendServerCallback {
		public void callback(Collection<String> patients);
	}
	
	private static final String SEP = ":" ;
	private static final String PREFIX = "com.ibm.streamsx.health" + SEP ;
	private static final String SERVICES_PREFIX = PREFIX + "services" + SEP;
	private static final String ALERT_RULES_KEY = PREFIX + "alert_rules";
	private static final String GLOBAL_PATIENTS_LIST_PREFIX = PREFIX + "patients";
	private static final String STATE_KEY = "state";
	private static final String TOPIC_KEY = "topics";
	private static final String PATIENT_KEY = "patients";
	
	private RedisClient client;
	private RedisCommands<String, String> sync;
	private String serviceName;
	private RedisPubSubCommands<String, String> pubSub;
	private RedisServerListener listener;
	
	public RedisServerAdapter(String serviceName, String connectionString, RedisServerListener listener) {
		setServiceName(serviceName);
		this.listener = listener;
		
		client = RedisClient.create("redis://" + connectionString);		
		sync = client.connect().sync();
		pubSub = client.connectPubSub().sync();

		// subscribe to updates to the patients enabled for this service
		pubSub.addListener(new MyRedisListeners(sync));
		String[] channels = {
				"__keyspace@0__:" + getServicePatientsListKey(),
				"__keyspace@0__:" + getAlertsKey()
		};
		pubSub.subscribe(channels);
	}
	
	@Override
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	@Override
	public void registerService(ServiceInfo info) {
		// add the service key to the index
		sync.sadd(getServicesIndexKey(), SERVICES_PREFIX + serviceName);
		
		// create an entry containing the service status
		sync.set(getServiceStatusKey(), info.toString());
	}

	@Override
	public void unregisterService() {
		// update the info for the service
		sync.set(getServiceStatusKey(), Constants.STOPPED_SERVICE.toString());
	}
	
	@Override
	public void registerServiceTopics(List<String> topics) {
		sync.sadd(getServiceTopicsKey(), topics.toArray(new String[0]));
	}

	@Override
	public void addPatientToGlobalList(String patientId) {
		sync.sadd(getGlobalPatientsListKey(), patientId);
	}

	@Override
	public void removePatientFromGlobalList(String patientId) {
		sync.srem(getGlobalPatientsListKey(), patientId);
	}

	@Override
	public void removeAllPatientsFromGlobalList() {
		sync.del(getGlobalPatientsListKey());
	}
	
	@Override
	public Collection<String> getServicePatients() {
		return sync.smembers(getServicePatientsListKey());
	}

	@Override
	public Collection<String> getAlerts() {
		return sync.smembers(getAlertsKey());
	}
	
	@Override
	public void close() {
		client.shutdown();
	}
	
	private String getGlobalPatientsListKey() {
		return GLOBAL_PATIENTS_LIST_PREFIX;
	}
	
	private String getServiceTopicsKey() {
		return SERVICES_PREFIX + serviceName + SEP + TOPIC_KEY;
	}
	
	private String getServiceStatusKey() {
		return SERVICES_PREFIX + serviceName + SEP + STATE_KEY;
	}
	
	private String getServicesIndexKey() {
		return SERVICES_PREFIX + "index";
	}
	
	private String getServicePatientsListKey() {
		return SERVICES_PREFIX + serviceName + SEP + PATIENT_KEY;
	}
	
	private String getAlertsKey() {
		return ALERT_RULES_KEY;
	}

	
	private class MyRedisListeners implements RedisPubSubListener<String, String> {

		private RedisCommands<String, String> sync;
		
		public MyRedisListeners(RedisCommands<String, String> sync) {
			this.sync = sync;
		}
		
		@Override
		public void message(String channel, String message) {
			if(message.equals("sadd") || message.equals("srem")) {
				String key = channel.split(":", 2)[1];
				if(key.equals(getServicePatientsListKey())) {
					Set<String> patients = sync.smembers(key);
					listener.patientsUpdated(patients);					
				} else if(key.equals(getAlertsKey())) {
					Set<String> alerts = sync.smembers(key);
					listener.alertRulesUpdated(alerts);
				}
			}
		}

		@Override
		public void message(String pattern, String channel, String message) {
		}

		@Override
		public void subscribed(String channel, long count) {
			System.out.println("subscribed: " + channel);
		}

		@Override
		public void psubscribed(String pattern, long count) {
			
		}

		@Override
		public void unsubscribed(String channel, long count) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void punsubscribed(String pattern, long count) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
