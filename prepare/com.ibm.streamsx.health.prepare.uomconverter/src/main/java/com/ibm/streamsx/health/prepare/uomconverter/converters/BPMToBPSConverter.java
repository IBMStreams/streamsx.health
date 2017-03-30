package com.ibm.streamsx.health.prepare.uomconverter.converters;

import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Frequency;

import com.ibm.streamsx.health.prepare.uomconverter.SupportedUOM;

import tec.units.ri.AbstractUnit;
import tec.units.ri.unit.Units;

@SupportedUOM(inputUOM={"bpm"}, outputUOM={"bps"})
public class BPMToBPSConverter extends AbstractUOMConverter {

	private UnitConverter converter;
	
	public BPMToBPSConverter(String inputUOM, String outputUOM) {
		super(inputUOM, outputUOM);
		
		Unit<Frequency> bpm = AbstractUnit.ONE.divide(Units.MINUTE).asType(Frequency.class);
		Unit<Frequency> bps = AbstractUnit.ONE.divide(Units.SECOND).asType(Frequency.class);
		
		converter = bpm.getConverterTo(bps);
	}

	@Override
	UnitConverter getConverter() {
		return converter;
	}

}
