/*******************************************************************************
* Copyright (C) 2014, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/
package com.ibm.streamsx.objectstorage.client;

import java.util.Properties;

import org.apache.hadoop.conf.Configuration;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streamsx.objectstorage.Utils;


public class ObjectStorageClientFactory {

	
	public static IObjectStorageClient getObjectStorageClient(String objectStorageURI, OperatorContext opContext, Properties appConfigCreds, Configuration config) throws Exception {
		String protocol = Utils.getProtocol(objectStorageURI);

		switch (protocol.toLowerCase()) {
		case Constants.S3A:
			return new ObjectStorageS3AClient(objectStorageURI, opContext, appConfigCreds, config);
		case Constants.COS:
			return new ObjectStorageCOSClient(objectStorageURI, opContext, appConfigCreds, config);
		case Constants.FILE:
			return new ObjectStorageFileClient(objectStorageURI, opContext, config);
		default:
			throw new IllegalArgumentException(
					"No Object Storage client implementation found for protocol '" + protocol.toLowerCase() + "'");
		}
		
	}
	
}
