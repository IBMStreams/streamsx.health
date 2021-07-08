package com.ibm.streamsx.objectstorage.s3;


/**
 * S3-specific authentication scheme interface
 * @author streamsadmin
 *
 */
public interface IS3ObjectStorageAuth {

	public void setAccessKeyID(String accessKeyID);
	
	public void setSecretAccessKey(String secretAccessKey);
	
	public String getSecretAccessKey();
	
	public void setBucket(String bucket);
	
	public String getBucket();
	
	public void setProtocol(S3Protocol protocol);
	
	public S3Protocol getProtocol();
	
	public void setEndpoint(String endpoint);
	
	public String getEndpoint();
}
