package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;
import java.util.List;

public class Patient implements Serializable {
	
	private String id;
	private List<String> alternateIds;
	private String name;
	private String gender;
	private String dateOfBirth;
	private String status;

}
