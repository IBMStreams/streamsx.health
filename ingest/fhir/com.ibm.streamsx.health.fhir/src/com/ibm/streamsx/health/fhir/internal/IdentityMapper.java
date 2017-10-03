//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.internal;

import java.util.ArrayList;
import java.util.List;

import com.ibm.streamsx.topology.function.Function;

public class IdentityMapper<T> implements Function<T, Iterable<T>>{
	
	private static final long serialVersionUID = 1L;


	public Iterable<T> apply(T v) {
		List<T> params = new ArrayList<T>();
		params.add(v);
		
		return params;
	}

}
