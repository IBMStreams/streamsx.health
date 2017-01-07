package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Channel extends HashMap<String, ITerm> {

	private static final long serialVersionUID = 1L;

	public ITerm getTerm(String termName) {
		return this.get(termName);
	}
	
	public List<String> getTermNames() {
		return new ArrayList<>(keySet());
	}
}
