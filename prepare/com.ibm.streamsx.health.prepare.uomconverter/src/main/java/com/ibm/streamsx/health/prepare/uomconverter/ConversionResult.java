package com.ibm.streamsx.health.prepare.uomconverter;

public class ConversionResult {

	private String inputUOM;
	private String outputUOM;
	private double convertedValue;

	public ConversionResult(String inputUOM, String outputUOM, double convertedValue) {
		super();
		this.inputUOM = inputUOM;
		this.outputUOM = outputUOM;
		this.convertedValue = convertedValue;
	}

	public double getConvertedValue() {
		return convertedValue;
	}

	public String getInputUOM() {
		return inputUOM;
	}

	public String getOutputUOM() {
		return outputUOM;
	}
}
