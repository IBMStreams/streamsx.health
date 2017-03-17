/*******************************************************************************
 * Copyright (C) 2017 International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/
package com.ibm.streamsx.health.vines.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Terms extends HashMap<String, Channel> {

	private static final long serialVersionUID = 1L;
	
	public Channel getChannel(String channelName) {
		return this.get(channelName);
	}
	
	public List<String> getChannelNames() {
		return new ArrayList<>(keySet());
	}
}
