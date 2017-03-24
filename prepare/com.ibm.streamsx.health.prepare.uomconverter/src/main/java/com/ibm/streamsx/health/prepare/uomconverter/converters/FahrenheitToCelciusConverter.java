package com.ibm.streamsx.health.prepare.uomconverter.converters;

import javax.measure.UnitConverter;

import com.ibm.streamsx.health.prepare.uomconverter.SupportedUOM;

import systems.uom.common.USCustomary;
import tec.units.ri.unit.Units;

@SupportedUOM(inputUOM={"F", "°F"}, outputUOM={"C", "°C"})
public class FahrenheitToCelciusConverter extends AbstractUOMConverter {
	private UnitConverter converter;
	
	public FahrenheitToCelciusConverter(String inputUOM, String outputUOM) {
		super(inputUOM, outputUOM);		
		converter = USCustomary.FAHRENHEIT.getConverterTo(Units.CELSIUS);
	}

	@Override
	protected UnitConverter getConverter() {
		return converter;
	}
}
