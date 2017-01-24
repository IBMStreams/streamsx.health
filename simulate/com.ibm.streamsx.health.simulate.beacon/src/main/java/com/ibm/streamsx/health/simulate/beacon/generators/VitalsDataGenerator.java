package com.ibm.streamsx.health.simulate.beacon.generators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class VitalsDataGenerator implements Iterable<Double>, Iterator<Double>, Serializable {
	private static final long serialVersionUID = 1L;
	private Double min;
	//private Double max;
	private Double delta;
	private Double count;
	private Double speed = 1.0;
	
	private static final Double MIN_RAND_VALUE = 0.5;
	private static final Double MAX_RAND_VALUE = 1.0;
	
	public VitalsDataGenerator(Double min, Double max) {
		this.min = min;
		//this.max = max;
		delta = max - min;
		count = 0.0;
	}
	
	@Override
	public Iterator<Double> iterator() {
		return this;
	}
	@Override
	public boolean hasNext() {
		return true;
	}
	@Override
	public Double next() {
		count = (count == Double.MAX_VALUE) ? 0.0 : count+1.0;
		
		Double val = ThreadLocalRandom.current().nextDouble(MIN_RAND_VALUE, MAX_RAND_VALUE);
		return val * (delta/2.0) * Math.cos(Math.toRadians(count/(1/speed))) * Math.cos(Math.toRadians(count/(2/speed))) * Math.cos(Math.toRadians(count/(3/speed))) + min + delta/2.0;
//		Double val = ThreadLocalRandom.current().nextDouble(0.75, 1.0);
//		return val*(delta/2.0)*Math.sin(count/288) + min + delta/2.0;
	}
	
	public void setSpeed(Double speed) {
		this.speed = speed;
	}
	
}
