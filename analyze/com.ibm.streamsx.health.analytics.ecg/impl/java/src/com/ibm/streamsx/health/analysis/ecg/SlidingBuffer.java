package com.ibm.streamsx.health.analysis.ecg;

import java.util.ArrayList;

public class SlidingBuffer<T> extends ArrayList<T> {
	private static final long serialVersionUID = 1L;

	private int maxSize;
	
	public SlidingBuffer(int maxSize) {
		this.maxSize = maxSize;
	}
	
	@Override
	public boolean add(T e) {
		if(this.size() == maxSize) {
			remove(0);
		}
		return super.add(e);
	}	
}
