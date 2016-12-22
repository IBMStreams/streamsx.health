package com.ibm.streamsx.health.vines.model;

import java.util.ArrayList;
import java.util.List;

public class WaveformHelper {

	private static final String SEP = "\\^";
	
	public static List<Double> parseWaveform(String waveform) {
		List<Double> numericValues = new ArrayList<Double>();
		String[] values = waveform.split(SEP);
		for(String v : values) {
			numericValues.add(Double.valueOf(v));
		}
		
		return numericValues;
	}
	
}
