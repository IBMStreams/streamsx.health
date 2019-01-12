package com.ibm.streamsx.objectstorage.auth;

import java.net.URI;

import org.apache.hadoop.conf.Configuration;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.ibm.oauth.BasicIBMOAuthCredentials;
import com.ibm.streamsx.objectstorage.IObjectStorageConstants;
import com.ibm.streamsx.objectstorage.Utils;
import com.ibm.streamsx.objectstorage.client.Constants;

/**
 * Credentials provider for IAM authentication
 */
public class IAMOSCredentialsProvider implements AWSCredentialsProvider  {

	BasicIBMOAuthCredentials fCredentials;
	
	public IAMOSCredentialsProvider(URI osURI, Configuration conf) {
		// extract credentials from the operator context
		String apiKey = conf.get(Constants.OST_IAM_APIKEY_CONFIG_NAME);
		String serviceInstanceId = conf.get(Constants.OST_IAM_INSTANCE_ID_CONFIG_NAME);
		String tokenEndpoint = conf.get(Constants.OST_IAM_TOKEN_ENDPOINT_CONFIG_NAME);
    	
		// initialize BMX IAM credentials object
		SDKGlobalConfiguration.IAM_ENDPOINT = tokenEndpoint;
        fCredentials = new BasicIBMOAuthCredentials(apiKey, serviceInstanceId);        
	}
	
	@Override
	public AWSCredentials getCredentials() {

		return fCredentials;
	}

	@Override
	public void refresh() {}

}
