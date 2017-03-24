package com.ibm.streamsx.health.prepare.uomconverter.converters;

import java.util.List;

import javax.measure.UnitConverter;
import javax.measure.format.UnitFormat;
import javax.measure.spi.ServiceProvider;

public abstract class AbstractUOMConverter implements UnitConverter {

	protected UnitFormat unitFormat;
	private String inputUOM;
	private String outputUOM;
	
	public AbstractUOMConverter(String inputUOM, String outputUOM) {
		unitFormat = ServiceProvider.current().getUnitFormatService().getUnitFormat();
		this.inputUOM = inputUOM;
		this.outputUOM = outputUOM;
	}

	public String getInputUOM() {
		return inputUOM;
	}
	
	public String getOutputUOM() {
		return outputUOM;
	}
	
	@Override
	public boolean isIdentity() {
		return getConverter().isIdentity();
	}

	@Override
	public boolean isLinear() {
		return getConverter().isLinear();
	}

	@Override
	public UnitConverter inverse() {
		return getConverter().inverse();
	}

	@Override
	public Number convert(Number value) {
		return getConverter().convert(value);
	}

	@Override
	public double convert(double value) {
		return getConverter().convert(value);
	}

	@Override
	public UnitConverter concatenate(UnitConverter converter) {
		return converter.concatenate(converter);
	}

	@Override
	public List<? extends UnitConverter> getConversionSteps() {
		return getConverter().getConversionSteps();
	}

	protected UnitFormat getUnitFormat() {
		return unitFormat;
	}

	abstract UnitConverter getConverter();
}
