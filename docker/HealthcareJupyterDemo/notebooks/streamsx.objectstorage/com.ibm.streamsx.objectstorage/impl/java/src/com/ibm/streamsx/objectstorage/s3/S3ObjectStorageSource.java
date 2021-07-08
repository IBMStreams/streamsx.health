package com.ibm.streamsx.objectstorage.s3;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.model.InputPortSet;
import com.ibm.streams.operator.model.InputPorts;
import com.ibm.streams.operator.model.Libraries;
import com.ibm.streams.operator.model.OutputPortSet;
import com.ibm.streams.operator.model.OutputPorts;
import com.ibm.streams.operator.model.Parameter;
import com.ibm.streams.operator.model.PrimitiveOperator;
import com.ibm.streamsx.objectstorage.BaseObjectStorageSource;
import com.ibm.streamsx.objectstorage.ObjectStorageSource;
import com.ibm.streamsx.objectstorage.Utils;
import com.ibm.streamsx.objectstorage.client.Constants;
import com.ibm.streams.operator.model.InputPortSet.WindowMode;
import com.ibm.streams.operator.model.InputPortSet.WindowPunctuationInputMode;
import com.ibm.streams.operator.model.OutputPortSet.WindowPunctuationOutputMode;


@PrimitiveOperator(name="S3ObjectStorageSource", namespace="com.ibm.streamsx.objectstorage.s3",
description=S3ObjectStorageSource.DESC+ObjectStorageSource.BASIC_DESC+S3ObjectStorageSource.EXAMPLES_DESC)
@InputPorts({@InputPortSet(description="The `S3ObjectStorageSource` operator has one optional input port. If an input port is specified, the operator expects an input tuple with a single attribute of type rstring. The input tuples contain the object names that the operator opens for reading. The input port is non-mutating.", cardinality=1, optional=true, windowingMode=WindowMode.NonWindowed, windowPunctuationInputMode=WindowPunctuationInputMode.Oblivious)})
@OutputPorts({@OutputPortSet(description="The `S3ObjectStorageSource` operator has one output port. The tuples on the output port contain the data that is read from the objects. The operator supports two modes of reading.  To read an object line-by-line, the expected output schema of the output port is tuple<rstring line>. To read an object as binary, the expected output schema of the output port is tuple<blob data>. Use the blockSize parameter to control how much data to retrieve on each read. The operator includes a punctuation marker at the conclusion of each object.", cardinality=1, optional=false, windowPunctuationOutputMode=WindowPunctuationOutputMode.Generating)})
@Libraries({"opt/*","opt/downloaded/*" })
public class S3ObjectStorageSource extends BaseObjectStorageSource  implements IS3ObjectStorageAuth {

	public static final String DESC = 
			"Operator reads objects from S3 compliant object storage.";	
	
	public static final String EXAMPLES_DESC =
			"\\n"+
			"\\n+ Examples\\n"+
			"\\n"+
			"\\nThese examples use the `S3ObjectStorageSource` operator.\\n"+
			"\\n"+
			"\\n**a)** S3ObjectStorageSource with dynamic object names to be read\\n"+
			"\\n"+
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
			"\\n"+
			"\\n**b)** S3ObjectStorageSource with static object name to be read\\n"+
			"\\n"+
			"\\n    composite Main {"+
			"\\n        param"+
			"\\n            expression<rstring> $accessKeyID : getSubmissionTimeValue(\\\"os-access-key-id\\\");"+
			"\\n            expression<rstring> $secretAccessKey : getSubmissionTimeValue(\\\"os-secret-access-key\\\");"+
			"\\n            expression<rstring> $bucket: getSubmissionTimeValue(\\\"os-bucket\\\");"+
			"\\n            expression<rstring> $endpoint: getSubmissionTimeValue(\\\"os-endpoint\\\", \\\"s3-api.us-geo.objectstorage.softlayer.net\\\");"+
			"\\n        graph"+
			"\\n            // read text object"+			
			"\\n            // use a S3ObjectStorageSource operator with no input port to process a single object"+
			"\\n            stream<rstring line> TxtData = com.ibm.streamsx.objectstorage.s3::S3ObjectStorageSource() {"+
			"\\n                param"+
			"\\n                    accessKeyID : $accessKeyID;"+
			"\\n                    secretAccessKey : $secretAccessKey;"+
			"\\n                    bucket : $bucket;"+
			"\\n                    endpoint: $endpoint;"+
			"\\n                    objectName: \\\"sample.txt\\\";"+
			"\\n            }"+
			"\\n"+			
			"\\n            // read binary object"+			
			"\\n            // use a S3ObjectStorageSource operator with no input port to process a single object"+
			"\\n            stream<blob block> BinData = com.ibm.streamsx.objectstorage.s3::S3ObjectStorageSource() {"+
			"\\n                param"+
			"\\n                    accessKeyID : $accessKeyID;"+
			"\\n                    secretAccessKey : $secretAccessKey;"+
			"\\n                    bucket : $bucket;"+
			"\\n                    endpoint: $endpoint;"+
			"\\n                    objectName: \\\"sample.bin\\\";"+
			"\\n                    blockSize: 0; // loads file as a single tuple"+
			"\\n            }"+
			"\\n    }\\n"			
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

	@Parameter(optional=false, description = "Specifies a bucket to use for reading objects. The bucket must exist. The operator does not create a bucket.")	
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
