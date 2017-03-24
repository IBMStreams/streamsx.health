package com.ibm.streamsx.health.prepare.uomconverter;

import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.UnconvertibleException;

import org.apache.log4j.Logger;

import com.ibm.streamsx.health.prepare.uomconverter.converters.AbstractUOMConverter;

public class ConverterFactory implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<Class<? extends AbstractUOMConverter>> supportedConverters;
	private transient Map<String, AbstractUOMConverter> converterMap;
	
	private static Logger logger = Logger.getLogger("ConverterFactory");
	private static final String MAP_DELIM = "|";
	
	public ConverterFactory() {
		supportedConverters = new ArrayList<>();
		initConverterMap();
	}
	
	private Object readResolve() throws ObjectStreamException {
		System.out.println("SUPPORTED CONVERTERS SIZE=" + supportedConverters.size());
		initConverterMap();
		return this;
	}
	
	private void initConverterMap() {
		converterMap = new HashMap<String, AbstractUOMConverter>();
	}
	
	public void registerConverterClass(Class<? extends AbstractUOMConverter> converterClass) {
		logger.debug("Registering converter class: " + converterClass.getName());
		supportedConverters.add(converterClass);
	}
	
	public AbstractUOMConverter createConverter(String inputUOM, String outputUOM) throws NoConverterFoundException {
		for(Class<? extends AbstractUOMConverter> converterClass : supportedConverters) {
			SupportedUOM supportedUoms = converterClass.getAnnotation(SupportedUOM.class);
			List<String> inputUOMList = Arrays.asList(supportedUoms.inputUOM());
			List<String> outputUOMList = Arrays.asList(supportedUoms.outputUOM());
			if(inputUOMList.contains(inputUOM) && outputUOMList.contains(outputUOM)) {
				try {
					Constructor<? extends AbstractUOMConverter> constructor = converterClass.getConstructor(String.class, String.class);
					AbstractUOMConverter converter = constructor.newInstance(inputUOM, outputUOM);
					registerConverter(converter);
					return converter;
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}			
		}
		
		throw new NoConverterFoundException(inputUOM, outputUOM);
	}
	
	/**
	 * Reads the map file and creates converters for each entry. 
	 * Each line of the map file represents a single conversion. The map file should
	 * have the following format: 
	 * 
	 *   inputUOM->outputUOM
	 *   
	 * Where 'inputUOM' is the unit of measure that you want to convert from,
	 * and 'outputUOM' is the unit of measure that you want to convert to. For
	 * example, if you want to convert all values in milliVolts to Volts, you would
	 * specify the following in the map file: 
	 * 
	 *   mV->V
	 * 
	 * 
	 * @param filePath The path to the unit of measure map file
	 * @throws Exception
	 */
	public void createConvertersFromMapFile(String filePath) throws Exception {
		List<String> lines = Files.readAllLines(new File(filePath).toPath());
		for(String line : lines) {
			String[] tokens = line.split(MAP_DELIM);
			if(tokens.length == 2) {
				String inputUOM = tokens[0].trim();
				String outputUOM = tokens[1].trim();
				createConverter(inputUOM, outputUOM);
			}
		}
	}
	
	private void registerConverter(AbstractUOMConverter converter) {
		System.out.println("Registering converter: '" + converter.getClass().getName() + "' for UOM: '" + converter.getInputUOM() + "'");
		converterMap.put(converter.getInputUOM(), converter);
	}
	
	public ConversionResult convert(double value, String inputUOM) throws UnconvertibleException {
		AbstractUOMConverter converter = converterMap.get(inputUOM);
		if(converter == null) {
			throw new UnconvertibleException("No converter found for UOM: '" + inputUOM + "'");
		}

		double convertedValue = converter.convert(value);
		String outputUOM = converter.getOutputUOM();
		
		return new ConversionResult(inputUOM, outputUOM, convertedValue);
	}
	
	public boolean hasConverter(String inputUOM) {
		return converterMap.containsKey(inputUOM);
	}
}
