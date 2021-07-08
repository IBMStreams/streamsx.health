package com.ibm.streamsx.objectstorage.s3;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.model.InputPortSet;
import com.ibm.streams.operator.model.InputPorts;
import com.ibm.streams.operator.model.Libraries;
import com.ibm.streams.operator.model.OutputPortSet;
import com.ibm.streams.operator.model.OutputPorts;
import com.ibm.streams.operator.model.Parameter;
import com.ibm.streams.operator.model.PrimitiveOperator;
import com.ibm.streams.operator.model.InputPortSet.WindowMode;
import com.ibm.streams.operator.model.InputPortSet.WindowPunctuationInputMode;
import com.ibm.streamsx.objectstorage.BaseObjectStorageScan;
import com.ibm.streamsx.objectstorage.ObjectStorageScan;
import com.ibm.streamsx.objectstorage.Utils;
import com.ibm.streamsx.objectstorage.client.Constants;
import com.ibm.streams.operator.model.OutputPortSet.WindowPunctuationOutputMode;


@PrimitiveOperator(name = "S3ObjectStorageScan", namespace = "com.ibm.streamsx.objectstorage.s3",
description=S3ObjectStorageScan.DESC+ObjectStorageScan.BASIC_DESC+S3ObjectStorageScan.EXAMPLES_DESC)
@InputPorts({@InputPortSet(description="The `S3ObjectStorageSink` operator has an optional control input port. You can use this port to change the directory that the operator scans at run time without restarting or recompiling the application. The expected schema for the input port is of tuple<rstring directory>, a schema containing a single attribute of type rstring. If a directory scan is in progress when a tuple is received, the scan completes and a new scan starts immediately after and uses the new directory that was specified. If the operator is sleeping, the operator starts scanning the new directory immediately after it receives an input tuple.", cardinality=1, optional=true, windowingMode=WindowMode.NonWindowed, windowPunctuationInputMode=WindowPunctuationInputMode.Oblivious)})
@OutputPorts({
		@OutputPortSet(description = "The `S3ObjectStorageScan` operator has one output port. This port provides tuples of type rstring that are encoded in UTF-8 and represent the object names that are found in the directory, one object name per tuple. The object names do not occur in any particular order.", cardinality = 1, optional = false, windowPunctuationOutputMode = WindowPunctuationOutputMode.Free)})
@Libraries({"opt/*","opt/downloaded/*" })
public class S3ObjectStorageScan extends BaseObjectStorageScan implements IS3ObjectStorageAuth {

	public static final String DESC = 
			"Operator scans for specified key name pattern on a S3 compliant object storage.\\n" +
			"\\nThe `S3ObjectStorageScan` is similar to the `DirectoryScan` operator. "+
			"The `S3ObjectStorageScan` operator repeatedly scans an object storage directory and writes the names of new or modified files " +
			"that are found in the directory to the output port. The operator sleeps between scans.";	
	
	public static final String EXAMPLES_DESC =
			"\\n"+
			"\\n+ Examples\\n"+
			"\\n"+
			"\\nThis example use the `S3ObjectStorageScan` operator and `S3ObjectStorageSource` operator.\\n"+
			"\\n"+
			"\\nAs endpoint is the public **us-geo** (CROSS REGION) the default value of the `endpoint` submission parameter.\\n"+
			"\\n    composite Main {"+
			"\\n        param"+
			"\\n            expression<rstring> $accessKeyID : getSubmissionTimeValue(\\\"os-access-key-id\\\");"+
			"\\n            expression<rstring> $secretAccessKey : getSubmissionTimeValue(\\\"os-secret-access-key\\\");"+
			"\\n            expression<rstring> $bucket: getSubmissionTimeValue(\\\"os-bucket\\\");"+
			"\\n            expression<rstring> $endpoint: getSubmissionTimeValue(\\\"os-endpoint\\\", \\\"s3-api.us-geo.objectstorage.softlayer.net\\\");"+
			"\\n        graph"+
			"\\n            // S3ObjectStorageScan operator with directory and pattern"+
			"\\n            stream<rstring name> Scanned = com.ibm.streamsx.objectstorage.s3::S3ObjectStorageScan() {"+
			"\\n                param\\n"+
			"\\n                    accessKeyID : $accessKeyID;"+
			"\\n                    secretAccessKey : $secretAccessKey;"+
			"\\n                    bucket : $bucket;"+
			"\\n                    endpoint: $endpoint;"+
			"\\n                    directory: \\\"/sample\\\";"+
			"\\n                    pattern: \\\".*\\\";"+
			"\\n            }\\n"+
			"\\n            // use a S3ObjectStorageSource operator to process the object names"+
			"\\n            stream<rstring line> Data = com.ibm.streamsx.objectstorage.s3::S3ObjectStorageSource(Scanned) {"+
			"\\n                param"+
			"\\n                    accessKeyID : $accessKeyID;"+
			"\\n                    secretAccessKey : $secretAccessKey;"+
			"\\n                    bucket : $bucket;"+
			"\\n                    endpoint: $endpoint;"+
			"\\n            }"+
			"\\n    }\\n"+			
			"\\n"			
			;	
	
	private String fAccessKeyID;
	private String fsecretAccessKey;
	private String fBucket;
	private S3Protocol fProtocol = S3Protocol.s3a;
	
	@Override
	public void initialize(OperatorContext context) throws Exception {		
		setURI(Utils.getObjectStorageS3URI(getProtocol(), getBucket()));
		setUserID(getAccessKeyID());
		setPassword(getSecretAccessKey());
		setEndpoint((getEndpoint() == null) ? Constants.S3_DEFAULT_ENDPOINT : getEndpoint()); 
		super.initialize(context);
	}
	
	@Parameter(optional=false, description = "Specifies the Access Key ID for S3 account.")
	public void setAccessKeyID(String accessKeyID) {
		fAccessKeyID = accessKeyID;
	}
	
	public String getAccessKeyID() {
		return fAccessKeyID;
	}

	@Parameter(optional=false, description = "Specifies the Secret Access Key for S3 account.")
	public void setSecretAccessKey(String secretAccessKey) {
		fsecretAccessKey = secretAccessKey;
	}
	
	public String getSecretAccessKey() {
		return fsecretAccessKey;
	}

	@Parameter(optional=false, description = "Specifies a bucket to use for scanning. The bucket must exist. The operator does not create a bucket.")
	public void setBucket(String bucket) {
		fBucket = bucket;
	}
	
	public String getBucket() {
		return fBucket;
	}

	@Parameter(optional = true, description = "Specifies the protocol to use for communication with object storage. Supported values are s3a and cos. The default value is s3a.")
	public void setProtocol(S3Protocol protocol) {
		fProtocol = protocol;		
	}

	public S3Protocol getProtocol() {
		return fProtocol;
	}

	@Parameter(optional=true, description = "Specifies endpoint for connection to object storage. For example, for S3 the endpoint might be 's3.amazonaws.com'. The default value is the IBM Cloud Object Storage (COS) public endpoint 's3-api.us-geo.objectstorage.softlayer.net'.")
	public void setEndpoint(String endpoint) {
		super.setEndpoint(endpoint);
	}
	
}
