package com.ibm.streamsx.health.prepare.uomconverter.converters;

import javax.measure.UnitConverter;

import com.ibm.streamsx.health.prepare.uomconverter.SupportedUOM;

import systems.uom.common.USCustomary;
import tec.units.ri.unit.Units;

@SupportedUOM(inputUOM={"C", "°C"}, outputUOM={"F", "°F"})
public class CelciusToFahrenheitConverter extends AbstractUOMConverter {
	private UnitConverter converter;
	
	public CelciusToFahrenheitConverter(String inputUOM, String outputUOM) {
		super(inputUOM, outputUOM);
		converter = Units.CELSIUS.getConverterTo(USCustomary.FAHRENHEIT);
	}
	
	@Override
	protected UnitConverter getConverter() {
		return converter;
	}		
}
