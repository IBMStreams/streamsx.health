/*******************************************************************************
* Copyright (C) 2014, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/
package com.ibm.streamsx.objectstorage.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.Utils;
import com.ibm.streamsx.objectstorage.auth.OSAuthenticationHelper;


public class ObjectStorageFileClient extends ObjectStorageAbstractClient   {

	private static Logger TRACE = Logger.getLogger(ObjectStorageFileClient.class.getName());

	public ObjectStorageFileClient(String objectStorageURI, OperatorContext opContext) throws Exception {
		super(objectStorageURI, opContext, null);
	}

	public ObjectStorageFileClient(String objectStorageURI, OperatorContext opContext, Configuration config) throws Exception {
		super(objectStorageURI, opContext, null, config);
	}
	
	
	@Override
	public void initClientConfig() throws IOException, URISyntaxException {

		String protocol = Utils.getProtocol(fObjectStorageURI);

		// configure authentication related properties
		OSAuthenticationHelper.configAuthProperties(protocol, fOpContext, null, fConnectionProperties);
		
		// initialize COS specific properties
		fConnectionProperties.set(Constants.S3A_IMPL_CONFIG_NAME, Constants.LOCAL_DEFAULT_FS_IMPL);
	}

	
	@Override
	public void connect() throws Exception {
		initClientConfig();
		
	    fFileSystem = new org.apache.hadoop.fs.LocalFileSystem();
	    fFileSystem.initialize(new URI(fObjectStorageURI), fConnectionProperties);	
	    if (TRACE.isLoggable(TraceLevel.INFO)) {
	    	TRACE.log(TraceLevel.INFO, "Object storage client initialized with configuration: \n");
	    	for (Map.Entry<String, String> entry : fConnectionProperties) {
            	TRACE.log(TraceLevel.INFO, entry.getKey() + " = " + entry.getValue());
        	}
	    }
	}
}
