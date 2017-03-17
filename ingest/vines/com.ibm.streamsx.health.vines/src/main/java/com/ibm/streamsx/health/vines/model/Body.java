/*******************************************************************************
 * Copyright (C) 2017 International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/
package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

public class Body implements Serializable {

	private static final long serialVersionUID = 1L;

	private String Id;
	private String PlacerId;
	private String FillerId;
	private String DeviceId;
	private String TherapyId;
	private String StartTime;
	private String EndTime;
	private String UpdatedBy;
	private String UpdatedTime;
	private ServiceId ServiceId;
	private Terms Terms;

	public String getId() {
		return Id;
	}

	public String getDeviceId() {
		return DeviceId;
	}

	public String getStartTime() {
		return StartTime;
	}

	public String getEndTime() {
		return EndTime;
	}

	public String getUpdatedBy() {
		return UpdatedBy;
	}

	public String getUpdatedTime() {
		return UpdatedTime;
	}

	public ServiceId getServiceId() {
		return ServiceId;
	}

	public Terms getTerms() {
		return Terms;
	}

	public String getFillerId() {
		return FillerId;
	}

	public String getPlacerId() {
		return PlacerId;
	}

	public String getTherapyId() {
		return TherapyId;
	}

	@Override
	public String toString() {
		return "Body [Id=" + Id + ", PlacerId=" + PlacerId + ", FillerId=" + FillerId + ", DeviceId=" + DeviceId
				+ ", TherapyId=" + TherapyId + ", StartTime=" + StartTime + ", EndTime=" + EndTime + ", UpdatedBy="
				+ UpdatedBy + ", UpdatedTime=" + UpdatedTime + ", ServiceId=" + ServiceId + ", Terms=" + Terms + "]";
	}
	
	

}
