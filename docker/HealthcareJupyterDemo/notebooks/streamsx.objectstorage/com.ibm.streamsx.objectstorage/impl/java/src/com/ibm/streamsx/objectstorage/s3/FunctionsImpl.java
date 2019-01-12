//
// ****************************************************************************
// * Copyright (C) 2017, International Business Machines Corporation          *
// * All rights reserved.                                                     *
// ****************************************************************************
//

package com.ibm.streamsx.objectstorage.s3;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.ibm.oauth.BasicIBMOAuthCredentials;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ibm.streams.function.model.Function;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streams.toolkit.model.ToolkitLibraries;

/**
 * Class for implementing SPL Java native function. 
 */
@ToolkitLibraries({"opt/downloaded/*","opt/*"})
public class FunctionsImpl  {

	static Logger TRACER = Logger.getLogger("com.ibm.streamsx.objectstorage.s3");	

	private static AmazonS3 client = null;
	
	private static String S3_ERROR_MESSAGE = "Caught an AmazonClientException, which " +
            "means the client encountered " +
            "an internal error while trying to " +
            "communicate with S3, " +
            "such as not being able to access the network.\n"; 
	
	private static DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	

	@Function(namespace="com.ibm.streamsx.objectstorage.s3", name="initialize", description="Initialize S3 client using basic authentication. This method must be called first.", stateful=false)
    public static boolean initialize(String accessKeyID, String secretAccessKey, String endpoint) {
    	if (null == client) {
    		client = createClient(accessKeyID, secretAccessKey, endpoint, "us", false);
    	}
    	return true;
    }
    
	@Function(namespace="com.ibm.streamsx.objectstorage.s3", name="initialize_iam", description="Initialize S3 client using IAM credentials. This method must be called first.", stateful=false)
    public static boolean initialize_iam(String apiKey, String serviceInstanceId, String endpoint) {
    	if (null == client) {
    		client = createClient(apiKey, serviceInstanceId, endpoint, "us", true);
    	}
    	return true;
    }
    
    /**
     * @param apiKey (or accessKey)
     * @param serviceInstanceId (or secretKey)
     * @param endpoint
     * @param location
     * @return AmazonS3
     */
    public static AmazonS3 createClient(String apiKey, String serviceInstanceId, String endpoint, String location, boolean isIAM) {    	
		AWSCredentials credentials;
		if (isIAM) {
			credentials = new BasicIBMOAuthCredentials(apiKey, serviceInstanceId);
		}
		else {
			String accessKey = apiKey;
            String secretKey = serviceInstanceId;			
			credentials = new BasicAWSCredentials(accessKey, secretKey);
		}
		ClientConfiguration clientConfig = new ClientConfiguration().withRequestTimeout(5000);
		clientConfig.setUseTcpKeepAlive(true);

		return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(new EndpointConfiguration(endpoint, location)).withPathStyleAccessEnabled(true)
            .withClientConfiguration(clientConfig).build();
    }
	
    @Function(namespace="com.ibm.streamsx.objectstorage.s3", name="createBucket", description="Creates a bucket if it doesn't exist.", stateful=false)
    public static boolean createBucket(String bucket) {
    	return createBucket(bucket, "us-standard"); // create a standard bucket
    }
    
    
    @SuppressWarnings("deprecation")
	@Function(namespace="com.ibm.streamsx.objectstorage.s3", name="createBucket", description="Creates a bucket if it doesn't exist. Select with the locationConstraint argument the bucket type: Standard bucket (us-bucket), Vault bucket (us-vault) or Cold Vault bucket (us-cold)", stateful=false)
    public static boolean createBucket(String bucket, String locationConstraint) {
        boolean result = true;
        try {
        	if (!client.doesBucketExist(bucket)) {
        		client.createBucket(bucket, locationConstraint);
        	}
        }
        catch (AmazonClientException ace) {
        	if (!ace.getMessage().contains("BucketAlreadyExists")) {
        		result = false;
        		TRACER.log(TraceLevel.ERROR, S3_ERROR_MESSAGE + "ERROR: " + ace.getMessage());
        	}
        }
    	return result;
    }    
    
    @Function(namespace="com.ibm.streamsx.objectstorage.s3", name="deleteBucket", description="Deletes a bucket.", stateful=false)
    public static boolean deleteBucket(String bucket) {
        boolean result = true;
        try {
        	if (TRACER.isLoggable(TraceLevel.TRACE)) {
        		TRACER.log(TraceLevel.TRACE, "deleteBucket "+ bucket);
        	}
       		client.deleteBucket(bucket);
        }
        catch (AmazonClientException ace) {
        	result = false;
        	TRACER.log(TraceLevel.ERROR, S3_ERROR_MESSAGE + "ERROR: " + ace.getMessage());
        }
    	return result;
    }
    
    @Function(namespace="com.ibm.streamsx.objectstorage.s3", name="listBuckets", description="Lists all bucket names.", stateful=false)
    public static String[] listBuckets() {
    	String[] resultList = null;
        try {        	
        	List<Bucket> buckets = client.listBuckets(); // get a list of buckets
        	resultList = new String[buckets.size()];
        	int i = 0;
        	for (Bucket b : buckets) { // for each bucket...
        		resultList[i] = b.getName();
        		i++;
        	}
        }
        catch (AmazonClientException ace) {
        	resultList = null;
            TRACER.log(TraceLevel.ERROR, S3_ERROR_MESSAGE + "ERROR: " + ace.getMessage());
        }
    	return resultList;
    }
    
    @Function(namespace="com.ibm.streamsx.objectstorage.s3", name="listObjects", description="Lists all object names in a bucket.", stateful=false)
    public static String[] listObjects(String bucket) {
    	String[] resultList = null;
        try {
        	ObjectListing listing = client.listObjects(bucket);
        	List<S3ObjectSummary> summaries = listing.getObjectSummaries(); // create a list of object summaries
        	resultList = new String[summaries.size()];
        	int i = 0;
        	for (S3ObjectSummary obj : summaries){ // for each object...
        		resultList[i] = obj.getKey();
        		i++;
        	}
        }
        catch (AmazonClientException ace) {
        	resultList = null;
            TRACER.log(TraceLevel.ERROR, S3_ERROR_MESSAGE + "ERROR: " + ace.getMessage());
        }
    	return resultList;
    }    
    
    @Function(namespace="com.ibm.streamsx.objectstorage.s3", name="getObjectMetadata", description="Get object metadata.", stateful=false)
    public static String[] getObjectMetadata(String bucket, String objectName) {
    	String[] resultList = null;
        try {
        	ObjectMetadata objectMetadata = client.getObjectMetadata(bucket, objectName);        	        	
        	return new String[] {String.valueOf(objectMetadata.getContentLength()), DATE_FORMAT.format(objectMetadata.getLastModified())};
        }
        catch (AmazonClientException ace) {
        	resultList = null;
            TRACER.log(TraceLevel.ERROR, S3_ERROR_MESSAGE + "ERROR: " + ace.getMessage());
        }
    	return resultList;
    }    
    
    @Function(namespace="com.ibm.streamsx.objectstorage.s3", name="deleteAllObjects", description="Deletes all objects in a bucket.", stateful=false)
    public static boolean deleteAllObjects(String bucket) {
        boolean result = true;
        try {
        	ObjectListing listing = client.listObjects(bucket);
        	List<S3ObjectSummary> summaries = listing.getObjectSummaries(); // create a list of object summaries
        	for (S3ObjectSummary obj : summaries){ // for each object...
				deleteObject(obj.getKey(), bucket);
        	}
        }
        catch (AmazonClientException ace) {
        	result = false;
        	TRACER.log(TraceLevel.ERROR, S3_ERROR_MESSAGE + "ERROR: " + ace.getMessage());
        }
    	return result;
    }
    
    @Function(namespace="com.ibm.streamsx.objectstorage.s3", name="deleteObject", description="Deletes an object in a bucket.", stateful=false)
    public static boolean deleteObject(String objectName, String bucket) {
        boolean result = true;
        try {
        	TRACER.log(TraceLevel.TRACE, "deleteObject "+ objectName + " in bucket "+ bucket);
        	client.deleteObject(bucket, objectName);
        }
        catch (AmazonClientException ace) {
        	result = false;
        	TRACER.log(TraceLevel.ERROR, S3_ERROR_MESSAGE + "ERROR: " + ace.getMessage());
        }
    	return result;
    }
    
    @Function(namespace="com.ibm.streamsx.objectstorage.s3", name="getObjectStorageURI", description="Converts the bucket to a URI for the objectStorageURI parameter of the ObjectStorageSource, ObjectStorageScan and ObjectStorageSink operators.", stateful=false)
    public static String getObjectStorageURI(String bucket) {
    	return "s3a://"+bucket+"/";
    }
}
