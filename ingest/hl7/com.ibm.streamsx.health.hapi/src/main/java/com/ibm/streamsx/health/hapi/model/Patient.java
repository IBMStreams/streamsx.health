package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Patient implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String id = IHL7Constants.EMPTYSTR;
	private List<String> alternateIds = new ArrayList<String>();
	private String name = IHL7Constants.EMPTYSTR;
	private String gender = IHL7Constants.EMPTYSTR;
	private String dateOfBirth = IHL7Constants.EMPTYSTR;
	private String status = IHL7Constants.EMPTYSTR;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<String> getAlternateIds() {
		return alternateIds;
	}
	
	public void setAlternateIds(List<String> alternateIds) {
		this.alternateIds = alternateIds;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void addAlternativeId(String id)
	{
		alternateIds.add(id);
	}
	

}
