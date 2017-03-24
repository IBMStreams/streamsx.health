package com.ibm.streamsx.health.prepare.uomconverter.converters;

import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.ElectricPotential;
import javax.measure.spi.ServiceProvider;

import com.ibm.streamsx.health.prepare.uomconverter.SupportedUOM;

@SupportedUOM(inputUOM={"YV","ZV","EV","PV","TV","GV","MV","kV","hV","daV","dV","cV","mV","uV","µV","nV","pV","fV","aV","zV","yV"},
			  outputUOM={"YV","ZV","EV","PV","TV","GV","MV","kV","hV","daV","dV","cV","mV","uV","µV","nV","pV","fV","aV","zV","yV"})
public class VoltageConverter extends AbstractUOMConverter {

	private UnitConverter converter;
	
	public VoltageConverter(String inputUOM, String outputUOM) {
		super(inputUOM, outputUOM);
		unitFormat = ServiceProvider.current().getUnitFormatService().getUnitFormat();
		
		inputUOM = inputUOM.equals("uV") ? toMicrovolt(inputUOM) : inputUOM;
		outputUOM = outputUOM.equals("uV") ? toMicrovolt(outputUOM) : outputUOM;
		
		Unit<ElectricPotential> inUnit = unitFormat.parse(inputUOM).asType(ElectricPotential.class); 
		Unit<ElectricPotential> outUnit = unitFormat.parse(outputUOM).asType(ElectricPotential.class);
	
		converter = inUnit.getConverterTo(outUnit);
	}
	
	public String toMicrovolt(String uom) {
		return "µV";
	}
	
	@Override
	protected UnitConverter getConverter() {
		return converter;
	}
}
