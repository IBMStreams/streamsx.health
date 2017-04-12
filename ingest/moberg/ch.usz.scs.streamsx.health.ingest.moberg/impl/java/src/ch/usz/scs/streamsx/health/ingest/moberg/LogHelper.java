package ch.usz.scs.streamsx.health.ingest.moberg;

import com.ibm.streams.operator.OperatorContext;

public class LogHelper {
	public static String getMessage(OperatorContext context, String message) {
		return "Operator " + context.getName() + " " + message + " in PE: " + context.getPE().getPEId() + " in Job: " + context.getPE().getJobId();
	}
	
	public static final String INIT_MESSAGE = "initializing";
	public static final String PORTS_READY_MESSAGE = "all ports are ready";
	public static final String SHUTDOWN_MESSAGE = "shutting down";
}
