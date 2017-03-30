package com.ibm.streamsx.health.prepare.uomconverter.converters;

import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Frequency;

import com.ibm.streamsx.health.prepare.uomconverter.SupportedUOM;

import tec.units.ri.AbstractUnit;
import tec.units.ri.unit.Units;

@SupportedUOM(inputUOM={"bps"}, outputUOM={"bpm"})
public class BPSToBPMConverter extends AbstractUOMConverter {

	private UnitConverter converter;
	
	public BPSToBPMConverter(String inputUOM, String outputUOM) {
		super(inputUOM, outputUOM);
		
		Unit<Frequency> bpm = AbstractUnit.ONE.divide(Units.MINUTE).asType(Frequency.class);
		Unit<Frequency> bps = AbstractUnit.ONE.divide(Units.SECOND).asType(Frequency.class);
		
		converter = bps.getConverterTo(bpm);
	}

	@Override
	UnitConverter getConverter() {
		return converter;
	}

}
