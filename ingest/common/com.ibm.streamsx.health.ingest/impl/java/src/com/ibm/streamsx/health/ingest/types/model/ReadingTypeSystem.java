package com.ibm.streamsx.health.ingest.types.model;

public class ReadingTypeSystem {

	public static final String STREAMS_CODE_SYSTEM_NAMESPACE = "streamsx.heath";
	public static final String STREAMS_CODE_SYSTEM_VERSION = "1.0";
	public static final String STREAMS_CODE_SYSTEM = STREAMS_CODE_SYSTEM_NAMESPACE + "/" + STREAMS_CODE_SYSTEM_VERSION;

	public static final String UNKNOWN_CODE_SYSTEM = "unknown";
	
	/**
	 * Returns the namespace portion of the Streams Code System name
	 * @param system The Streams Code System name
	 * @return The namespace portion of the Systems Code System name, otherwise returns an empty string
	 */
	public static String getNamespace(String system) {
		String[] tokens = system.split("/");
		if(tokens.length > 0)
			return tokens[0];
		
		return "";
	}

	/**
	 * Returns the version portion of the Streams Code System name
	 * @param system The Streams Code System name
	 * @return The version portion of the Systems Code System name, otherwise returns an empty string
	 */
	public static String getVersion(String system) {
		String[] tokens = system.split("/");
		if(tokens.length > 1)
			return tokens[1];
		
		return "";
	}
	
}
