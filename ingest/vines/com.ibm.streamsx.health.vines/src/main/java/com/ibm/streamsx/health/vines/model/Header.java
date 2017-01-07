package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

public class Header implements Serializable {

	private static final long serialVersionUID = 1L;

	private String DeviceId;
	private String GroupId;
	private String ObjectSubType;
	private String TransationId;
	private Boolean IsCompleted;

	public String getDeviceId() {
		return DeviceId;
	}

	public String getGroupId() {
		return GroupId;
	}

	public String getObjectSubType() {
		return ObjectSubType;
	}

	public String getTransationId() {
		return TransationId;
	}

	public Boolean getIsCompleted() {
		return IsCompleted;
	}

	@Override
	public String toString() {
		return "Header [DeviceId=" + DeviceId + ", GroupId=" + GroupId + ", ObjectSubType=" + ObjectSubType
				+ ", TransationId=" + TransationId + ", IsCompleted=" + IsCompleted + "]";
	}

	

}
