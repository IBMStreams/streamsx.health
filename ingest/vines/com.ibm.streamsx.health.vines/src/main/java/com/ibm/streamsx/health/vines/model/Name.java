package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

public class Name implements Serializable {

	private static final long serialVersionUID = 1L;

	private String FirstName;
	private String LastName;
	private String MiddleName;
	private String Prefix;
	private String Suffix;

	public String getFirstName() {
		return FirstName;
	}

	public String getLastName() {
		return LastName;
	}

	public String getMiddleName() {
		return MiddleName;
	}

	public String getPrefix() {
		return Prefix;
	}

	public String getSuffix() {
		return Suffix;
	}

	@Override
	public String toString() {
		return "Name [FirstName=" + FirstName + ", LastName=" + LastName + ", MiddleName=" + MiddleName + ", Prefix="
				+ Prefix + ", Suffix=" + Suffix + "]";
	}

}
