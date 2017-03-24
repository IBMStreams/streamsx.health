package com.ibm.streamsx.health.prepare.uomconverter;

public class NoConverterFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoConverterFoundException() {
		// TODO Auto-generated constructor stub
	}
	
	public NoConverterFoundException(String inputUOM, String outputUOM) {
		super("Could not find any registered converters to support the UOM conversion: '" + inputUOM + "'->'" + outputUOM + "'");
	}
	
}
