package com.ibm.streamsx.objectstorage.auth;

import java.util.Properties;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.AbstractObjectStorageOperator;
import com.ibm.streamsx.objectstorage.IObjectStorageConstants;
import com.ibm.streamsx.objectstorage.Utils;
import com.ibm.streamsx.objectstorage.client.Constants;

/**
 * Contains logic for authentication related 
 * configuration settings
 * 
 * @author streamsadmin
 *
 */
public class OSAuthenticationHelper  {

	private static Logger TRACE = Logger.getLogger(OSAuthenticationHelper.class.getName());

	/**
	 * Initializes authentication configuration properties 
	 * based on relevant operator parameters
	 * 
	 * @param fOpContext operator context containing, among others,  authentication related parameters
	 * @param fConnectionProperties connection properties to initialize
	 */
	public static void configAuthProperties(final String protocol, final OperatorContext opContext, final Properties appConfigCreds, Configuration connectionProps) {
		AuthenticationType authType = getAuthenticationType(opContext);

		switch (protocol.toLowerCase()) {
		case Constants.S3A:
			initS3AAuth(authType, opContext, appConfigCreds, connectionProps);
			break;
		case Constants.COS:
			initCOSAuth(authType, opContext, appConfigCreds, connectionProps);
			break;
		case Constants.FILE:			
			// no authentication required
			break;
		default:
			throw new IllegalArgumentException(
					"Authentication properties can't be initialized for protocol '" + protocol.toLowerCase() + "'");
		}
	}
	
	/**
	 * Initializes set of authentication specific parameters for COS
	 * @param authType authentication type BASIC or IAM
	 * @param opContext operator context
	 * @param connectionProps connection properties from configuration
	 */
	private static void initCOSAuth(AuthenticationType authType, OperatorContext opContext, Properties appConfigCreds, Configuration connectionProps) {
		switch (authType) {
		case BASIC:
			TRACE.log(TraceLevel.INFO,	"initCOSAuth BASIC");
			if (opContext.getParameterNames().contains(IObjectStorageConstants.PARAM_OS_USER)) {
				connectionProps.set(Constants.COS_SERVICE_ACCESS_KEY_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_OS_USER, null));
				connectionProps.set(Constants.COS_SERVICE_SECRET_KEY_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_OS_PASSWORD, null));
			}
			else {
				connectionProps.set(Constants.COS_SERVICE_ACCESS_KEY_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_ACCESS_KEY_ID, null));
				connectionProps.set(Constants.COS_SERVICE_SECRET_KEY_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_SECRET_ACCESS_KEY, null));
			}
			break;
		case IAM: 	
			TRACE.log(TraceLevel.INFO,	"initCOSAuth IAM");
			if (null != appConfigCreds) {
				TRACE.log(TraceLevel.INFO,	"Set IAM credentials from application configuration");
				connectionProps.set(Constants.COS_SERVICE_IAM_APIKEY_CONFIG_NAME, appConfigCreds.getProperty(IObjectStorageConstants.PARAM_IAM_APIKEY));
				connectionProps.set(Constants.COS_SERVICE_IAM_SERVICE_IINSTANCE_ID_CONFIG_NAME, appConfigCreds.getProperty(IObjectStorageConstants.PARAM_IAM_SERVICE_INSTANCE_ID));
				connectionProps.set(Constants.COS_SERVICE_IAM_ENDPOINT_CONFIG_NAME, appConfigCreds.getProperty(IObjectStorageConstants.PARAM_IAM_TOKEN_ENDPOINT));
			}
			else {
				connectionProps.set(Constants.COS_SERVICE_IAM_APIKEY_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_IAM_APIKEY, null));
				connectionProps.set(Constants.COS_SERVICE_IAM_SERVICE_IINSTANCE_ID_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_IAM_SERVICE_INSTANCE_ID, null));
				connectionProps.set(Constants.COS_SERVICE_IAM_ENDPOINT_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_IAM_TOKEN_ENDPOINT, AbstractObjectStorageOperator.defaultIAMTokenEndpoint));
			}
			
			break;
		default: 		
			throw new IllegalArgumentException(
					"Unknown authentication method '" + authType + "' has been provided. Supported methods are: '" + authType.BASIC + "' and '" + authType.IAM + "'");			
		}
		
	}

	/**
	 * Initializes set of authentication specific parameters for S3
	 * @param authType authentication type BASIC or IAM
	 * @param opContext operator context
	 * @param connectionProps connection properties from configuration
	 */
	private static void initS3AAuth(AuthenticationType authType, OperatorContext opContext, Properties appConfigCreds, Configuration connectionProps) {
		switch (authType) {
		case BASIC: 
			TRACE.log(TraceLevel.INFO,	"initS3AAuth BASIC");
			if (opContext.getParameterNames().contains(IObjectStorageConstants.PARAM_OS_USER)) {
				connectionProps.set(Constants.S3A_SERVICE_ACCESS_KEY_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_OS_USER, null));
				connectionProps.set(Constants.S3A_SERVICE_SECRET_KEY_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_OS_PASSWORD, null));
			}
			else {
				connectionProps.set(Constants.S3A_SERVICE_ACCESS_KEY_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_ACCESS_KEY_ID, null));
				connectionProps.set(Constants.S3A_SERVICE_SECRET_KEY_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_SECRET_ACCESS_KEY, null));				
			}
			break;
		case IAM:
			TRACE.log(TraceLevel.INFO,	"initS3AAuth IAM");
			if (null != appConfigCreds) {
				TRACE.log(TraceLevel.INFO,	"Set IAM credentials from application configuration");
				connectionProps.set(Constants.OST_IAM_APIKEY_CONFIG_NAME, appConfigCreds.getProperty(IObjectStorageConstants.PARAM_IAM_APIKEY));
				connectionProps.set(Constants.OST_IAM_INSTANCE_ID_CONFIG_NAME, appConfigCreds.getProperty(IObjectStorageConstants.PARAM_IAM_SERVICE_INSTANCE_ID));
				connectionProps.set(Constants.OST_IAM_TOKEN_ENDPOINT_CONFIG_NAME, appConfigCreds.getProperty(IObjectStorageConstants.PARAM_IAM_TOKEN_ENDPOINT));
			}
			else {
				connectionProps.set(Constants.OST_IAM_APIKEY_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_IAM_APIKEY, null));
				connectionProps.set(Constants.OST_IAM_INSTANCE_ID_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_IAM_SERVICE_INSTANCE_ID, null));
				connectionProps.set(Constants.OST_IAM_TOKEN_ENDPOINT_CONFIG_NAME, Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_IAM_TOKEN_ENDPOINT, AbstractObjectStorageOperator.defaultIAMTokenEndpoint));
			}
			connectionProps.set(Constants.OST_IAM_CREDENTIALS_PROVIDER_CLASS_NAME, "com.ibm.streamsx.objectstorage.auth.IAMOSCredentialsProvider");
			break;
		default: 		
			throw new IllegalArgumentException(
					"Unknown authentication method '" + authType + "' has been provided. Supported methods are: '" + authType.BASIC + "' and '" + authType.IAM + "'");			
		}
	}
	
	/**
	 * Detects authentication type
	 * @param opContext
	 * @return authentication type: BASIC for user-based, IAM for token based
	 */
	public static AuthenticationType getAuthenticationType(OperatorContext opContext) {
		return (opContext.getParameterNames().contains(IObjectStorageConstants.PARAM_OS_USER) || opContext.getParameterNames().contains(IObjectStorageConstants.PARAM_ACCESS_KEY_ID) || opContext.getParameterNames().contains(IObjectStorageConstants.PARAM_USER_ID)) ? AuthenticationType.BASIC : AuthenticationType.IAM;
	}

}
