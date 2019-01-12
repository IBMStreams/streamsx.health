package com.ibm.streamsx.objectstorage;

import com.ibm.streams.operator.model.InputPortSet;
import com.ibm.streams.operator.model.InputPorts;
import com.ibm.streams.operator.model.Libraries;
import com.ibm.streams.operator.model.OutputPortSet;
import com.ibm.streams.operator.model.OutputPorts;
import com.ibm.streams.operator.model.Parameter;
import com.ibm.streams.operator.model.PrimitiveOperator;
import com.ibm.streams.operator.model.InputPortSet.WindowMode;
import com.ibm.streams.operator.model.InputPortSet.WindowPunctuationInputMode;
import com.ibm.streams.operator.model.OutputPortSet.WindowPunctuationOutputMode;

@PrimitiveOperator(name = "ObjectStorageScan", namespace = "com.ibm.streamsx.objectstorage",
description=ObjectStorageScan.DESC+ObjectStorageScan.BASIC_DESC+AbstractObjectStorageOperator.AUTHENTICATION_DESC+ObjectStorageScan.EXAMPLES_DESC)
@InputPorts({@InputPortSet(description="The `ObjectStorageScan` operator has an optional control input port. You can use this port to change the directory that the operator scans at run time without restarting or recompiling the application. The expected schema for the input port is of tuple<rstring directory>, a schema containing a single attribute of type rstring. If a directory scan is in progress when a tuple is received, the scan completes and a new scan starts immediately after and uses the new directory that was specified. If the operator is sleeping, the operator starts scanning the new directory immediately after it receives an input tuple.", cardinality=1, optional=true, windowingMode=WindowMode.NonWindowed, windowPunctuationInputMode=WindowPunctuationInputMode.Oblivious)})
@OutputPorts({
		@OutputPortSet(description = "The `ObjectStorageScan` operator has one output port. This port provides tuples of type rstring that are encoded in UTF-8 and represent the object names that are found in the directory, one object name per tuple. The object names do not occur in any particular order.", cardinality = 1, optional = false, windowPunctuationOutputMode = WindowPunctuationOutputMode.Free)})
@Libraries({"opt/*","opt/downloaded/*" })
public class ObjectStorageScan extends BaseObjectStorageScan implements IObjectStorageAuth {

	public static final String DESC = 
			"Operator scans for specified key name pattern on a object storage. The operator supports basic (HMAC) and IAM authentication.\\n" +
			"\\nThe `ObjectStorageScan` is similar to the `DirectoryScan` operator. "+
			"The `ObjectStorageScan` operator repeatedly scans an object storage directory and writes the names of new or modified files " +
			"that are found in the directory to the output port. The operator sleeps between scans.";
	
	public static final String BASIC_DESC = 					
			"\\n"+
			"\\n# Behavior in a consistent region\\n" +
			"\\nThe operator can participate in a consistent region. " +
			"The operator can be at the start of a consistent region if there is no input port.\\n" +
			"\\nThe operator supports periodic and operator-driven consistent region policies. " +
			"If consistent region policy is set as operator driven, the operator initiates a drain after each tuple is submitted. " +
			"\\nThis allows for a consistent state to be established after a object is fully processed.\\n" +
			"If the consistent region policy is set as periodic, the operator respects the period setting and establishes consistent states accordingly. " +
			"This means that multiple objects can be processed before a consistent state is established.\\n" +
			"\\nAt checkpoint, the operator saves the last submitted object name and its modification timestamp to the checkpoint." +
			"\\nUpon application failures, the operator resubmits all objects that are newer than the last submitted object at checkpoint."
		   	;	
	
	public static final String EXAMPLES_DESC =
			"\\n"+
			"\\n+ Examples\\n"+
			"\\n"+
			"\\nThese examples use the `ObjectStorageScan` operator.\\n"+
			"\\n"+
			"\\n**a)** Sample using `bucket` as submission parameter and `cos` **application configuration** with property `cos.creds` to specify the IAM credentials:\\n"+
			"\\nAs endpoint is the public **us-geo** (CROSS REGION) the default value of the `endpoint` submission parameter.\\n"+
			"\\n    composite Main {"+
			"\\n        param"+
			"\\n            expression<rstring> $bucket: getSubmissionTimeValue(\\\"os-bucket\\\");"+
			"\\n            expression<rstring> $endpoint: getSubmissionTimeValue(\\\"os-endpoint\\\", \\\"s3-api.us-geo.objectstorage.softlayer.net\\\");"+
			"\\n        graph"+
			"\\n            // ObjectStorageScan operator with directory and pattern"+
			"\\n            stream<rstring name> Scanned = com.ibm.streamsx.objectstorage::ObjectStorageScan() {"+
			"\\n                param\\n"+
			"\\n                    objectStorageURI: com.ibm.streamsx.objectstorage.s3::getObjectStorageURI($bucket);"+
			"\\n                    endpoint: $endpoint;"+
			"\\n                    directory: \\\"/sample\\\";"+
			"\\n                    pattern: \\\".*\\\";"+
			"\\n            }\\n"+
			"\\n            // use a ObjectStorageSource operator to process the object names"+
			"\\n            stream<rstring line> Data = com.ibm.streamsx.objectstorage::ObjectStorageSource(Scanned) {"+
			"\\n                param"+
			"\\n                    objectStorageURI: com.ibm.streamsx.objectstorage.s3::getObjectStorageURI($bucket);"+
			"\\n                    endpoint: $endpoint;"+
			"\\n            }"+
			"\\n    }\\n"+			
			"\\n"+
			"\\n**b)** Sample using parameters to specify the IAM credentials:\\n"+
			"\\nSet the **objectStorageURI** either in format \\\"cos://<bucket-name>/\\\" or \\\"s3a://<bucket-name>/\\\".\\n"+
			"\\n    composite Main {"+
			"\\n        param"+
			"\\n            expression<rstring> $IAMApiKey: getSubmissionTimeValue(\\\"os-iam-api-key\\\");"+
			"\\n            expression<rstring> $IAMServiceInstanceId: getSubmissionTimeValue(\\\"os-iam-service-instance\\\");"+
			"\\n            expression<rstring> $objectStorageURI: getSubmissionTimeValue(\\\"os-uri\\\");"+
			"\\n            expression<rstring> $endpoint: getSubmissionTimeValue(\\\"os-endpoint\\\", \\\"s3-api.us-geo.objectstorage.softlayer.net\\\");"+
			"\\n        graph"+
			"\\n            // ObjectStorageScan operator with directory and pattern"+
			"\\n            stream<rstring name> Scanned = com.ibm.streamsx.objectstorage::ObjectStorageScan() {"+
			"\\n                param\\n"+
			"\\n                    IAMApiKey: $IAMApiKey;"+
			"\\n                    IAMServiceInstanceId: $IAMServiceInstanceId;"+
			"\\n                    objectStorageURI: $objectStorageURI;"+
			"\\n                    endpoint: $endpoint;"+
			"\\n                    directory: \\\"/sample\\\";"+
			"\\n                    pattern: \\\".*\\\";"+
			"\\n            }\\n"+
			"\\n            // use a ObjectStorageSource operator to process the object names"+
			"\\n            stream<rstring line> Data = com.ibm.streamsx.objectstorage::ObjectStorageSource(Scanned) {"+
			"\\n                param"+
			"\\n                    IAMApiKey: $IAMApiKey;"+
			"\\n                    IAMServiceInstanceId: $IAMServiceInstanceId;"+
			"\\n                    objectStorageURI: $objectStorageURI;"+
			"\\n                    endpoint: $endpoint;"+
			"\\n            }"+
			"\\n    }\\n"			
			;
	
	@Parameter(optional=true, description = "Specifies username for connection to a Cloud Object Storage (COS), also known as 'AccessKeyID' for S3-compliant COS.")
	public void setObjectStorageUser(String objectStorageUser) {
		super.setUserID(objectStorageUser);
	}
	
	public String getObjectStorageUser() {
		return super.getUserID();
	}
	
	@Parameter(optional=true, description = "Specifies password for connection to a Cloud Object Storage (COS), also known as 'SecretAccessKey' for S3-compliant COS.")
	public void setObjectStoragePassword(String objectStoragePassword) {
		super.setPassword(objectStoragePassword);
	}
	
	public String getObjectStoragePassword() {
		return super.getPassword();
	}
	
	@Parameter(optional=false, description = "Specifies URI for connection to Cloud Object Storage (COS). For S3-compliant COS the URI should be in 'cos://bucket/ or s3a://bucket/' format. The bucket or container must exist. The operator does not create a bucket or container.")
	public void setObjectStorageURI(String objectStorageURI) {
		super.setURI(objectStorageURI);
	}
	
	public String getObjectStorageURI() {
		return super.getURI();
	}

	@Parameter(optional=false, description = "Specifies endpoint for connection to Cloud Object Storage (COS). For example, for S3 the endpoint might be 's3.amazonaws.com'.")
	public void setEndpoint(String endpoint) {
		super.setEndpoint(endpoint);
	}

	@Parameter(optional=true, description = "Specifies IAM API Key. Relevant for IAM authentication case only. If `cos` application configuration contains property `cos.creds`, then this parameter is ignored.")
	public void setIAMApiKey(String iamApiKey) {
		super.setIAMApiKey(iamApiKey);
	}
	
	public String getIAMApiKey() {
		return super.getIAMApiKey();
	}
	
	@Parameter(optional=true, description = "Specifies IAM token endpoint. Relevant for IAM authentication case only. Default value is 'https://iam.bluemix.net/oidc/token'.")
	public void setIAMTokenEndpoint(String iamTokenEndpoint) {
		super.setIAMTokenEndpoint(iamTokenEndpoint);;
	}
	
	public String getIAMTokenEndpoint() {
		return super.getIAMTokenEndpoint();
	}
	
	@Parameter(optional=true, description = "Specifies IAM service instance ID for connection to Cloud Object Storage (COS). Relevant for IAM authentication case only. If `cos` application configuration contains property `cos.creds`, then this parameter is ignored.")
	public void setIAMServiceInstanceId(String iamServiceInstanceId) {
		super.setIAMServiceInstanceId(iamServiceInstanceId);
	}
	
	public String getIAMServiceInstanceId() {
		return super.getIAMServiceInstanceId();
	}
	
	@Parameter(optional=true, description = "Specifies the name of the application configuration containing IBM Cloud Object Storage (COS) IAM credentials. If not set the default application configuration name is `cos`. Create a property in the `cos` application configuration *named* `cos.creds`. The *value* of the property `cos.creds` should be the raw IBM Cloud Object Storage Credentials JSON.")
	public void setAppConfigName(String appConfigName) {
		super.setAppConfigName(appConfigName);
	}
	
	public String getAppConfigName() {
		return super.getAppConfigName();
	}
	
}
