package com.ibm.streamsx.health.vines.test;

import java.util.HashMap;
import java.util.Map;

import com.ibm.streamsx.health.vines.service.VinesAdapterService;
import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContext.Type;

public class ServiceTest {

	public static void main(String[] args) throws Exception {
		Map<String, Object> rabbitMQParams = new HashMap<>();
		rabbitMQParams.put("hostAndPort", "REPLACE_ME:5673");
		rabbitMQParams.put("username", "guest");
		rabbitMQParams.put("password", "guest");
		rabbitMQParams.put("queueName", "vines");
		
		Map<String, Object> config = new HashMap<String, Object>();
		config.put(ContextProperties.SUBMISSION_PARAMS, rabbitMQParams);
		
		new VinesAdapterService().run(Type.DISTRIBUTED, config);
	}
}
