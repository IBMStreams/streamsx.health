package com.ibm.streamsx.health.vines.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.ibm.streamsx.health.ingest.types.connector.PublishConnector;
import com.ibm.streamsx.health.vines.VinesParser;
import com.ibm.streamsx.health.vines.VinesToChefMapper;
import com.ibm.streamsx.health.vines.model.Vines;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.spl.SPL;
import com.ibm.streamsx.topology.spl.SPLSchemas;

/**
 * Service for ingesting ViNES data from RabbitMQ.
 * 
 * Submission Time Parameters: 
 *  hostAndPort - specifies the host:port for the RabbitMQ server
 *  username - specifies the username to connect to RabbitMQ
 * 	 password - specifies the password to connect to RabbitMQ
 *  queue - specifies the RabbitMQ queue
 *  exchangeName - specifies the RabbitMQ exchange name. Default is "". 
 * 
 * Export Topic
 *  VINES_TOPIC = "ingest-vines"
 *  
 * Data Schema: 
 *  Data is exported as a ViNES Java object
 *  
 * 
 * @author streamsadmin
 *
 */
public class VinesAdapterService {
	
	public static final String VINES_TOPIC = "ingest-vines";
	
	private Topology topo;
	
	public VinesAdapterService() throws Exception {
		topo = new Topology("ViNESAdapter");
		
		topo.addJarDependency(System.getenv("STREAMS_INSTALL") + "/ext/lib/commons-lang-2.4.jar");
		topo.addJarDependency(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streamsx.datetime/impl/lib/com.ibm.streamsx.datetime.jar");
		
		SPL.addToolkit(topo, new File(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streamsx.messaging"));
		SPL.addToolkit(topo, new File(System.getenv("STREAMS_INSTALL") + "/toolkits/com.ibm.streamsx.json"));
		SPL.addToolkit(topo, new File("../../../com.ibm.streamsx.health.ingest"));
		
		Map<String, Object> rabbitMQParams = new HashMap<String, Object>();
		rabbitMQParams.put("hostAndPort", topo.createSubmissionParameter("hostAndPort", String.class));
		rabbitMQParams.put("exchangeName", topo.createSubmissionParameter("exchangeName", ""));
		rabbitMQParams.put("username", topo.createSubmissionParameter("username", String.class));
		rabbitMQParams.put("password", topo.createSubmissionParameter("password", String.class));
		rabbitMQParams.put("queueName", topo.createSubmissionParameter("queueName", String.class));
		rabbitMQParams.put("messageAttribute", SPLSchemas.STRING.getAttribute(0).getName());
		
		TStream<String> srcStream = SPL.invokeSource(topo, "com.ibm.streamsx.messaging.rabbitmq::RabbitMQSource", rabbitMQParams, SPLSchemas.STRING).toStringStream();
		TStream<Vines> vinesStream = srcStream.transform(VinesParser::fromJson);
		
		PublishConnector<Vines> connector = new PublishConnector<>(new VinesToChefMapper(), VINES_TOPIC);
		connector.mapAndPublish(vinesStream);
	}
	
	public Topology getTopology() {
		return topo;
	}
	
	public Object run(Type contextType, Map<String, Object> config) throws Exception {
		return StreamsContextFactory.getStreamsContext(contextType).submit(topo, config).get();
	}
}
