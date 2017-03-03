package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Vines implements Serializable {

	private static final long serialVersionUID = 1L;

	@SerializedName("_id")
	private RootId _id;

	private Data Data;

	@Expose(serialize = false, deserialize = false)
	private String rawMessage;
	
	public RootId get_id() {
		return _id;
	}

	public Data getData() {
		return Data;
	}

	public void set_id(RootId _id) {
		this._id = _id;
	}
	
	public void setData(Data data) {
		Data = data;
	}
	
	@Override
	public String toString() {
		return "ViNES [_id=" + _id + ", Data=" + Data + "]";
	}

	public void setRawMessage(String rawMessage) {
		this.rawMessage = rawMessage;
	}
	
	public String getRawMessage() {
		return rawMessage;
	}
	
}
