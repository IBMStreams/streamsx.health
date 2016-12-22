package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class Vines implements Serializable {

	private static final long serialVersionUID = 1L;

	@SerializedName("_id")
	private RootId _id;

	private Data Data;

	public RootId get_id() {
		return _id;
	}

	public Data getData() {
		return Data;
	}
	
	@Override
	public String toString() {
		return "ViNES [_id=" + _id + ", Data=" + Data + "]";
	}

}
