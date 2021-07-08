package com.ibm.streamsx.objectstorage.client;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.commons.io.FileUtils;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.Messages;
import com.ibm.streamsx.objectstorage.Utils;
import com.ibm.streamsx.objectstorage.auth.OSAuthenticationHelper;

public class ObjectStorageS3AClient extends ObjectStorageAbstractClient  {

	private static Logger TRACE = Logger.getLogger(ObjectStorageS3AClient.class.getName());


	public ObjectStorageS3AClient(String objectStorageURI, OperatorContext opContext, Properties appConfigCreds) throws Exception {
		super(objectStorageURI, opContext, appConfigCreds);
	}

	public ObjectStorageS3AClient(String objectStorageURI, OperatorContext opContext, Properties appConfigCreds, Configuration config) throws Exception {
		super(objectStorageURI, opContext, appConfigCreds, config);
	}

	@Override
	public void connect() throws Exception {
		initClientConfig();
		
	    fFileSystem = new org.apache.hadoop.fs.s3a.S3AFileSystem();	
		String formattedPropertyName = Utils.formatProperty(Constants.S3_SERVICE_ENDPOINT_CONFIG_NAME, Utils.getProtocol(fObjectStorageURI));
		String endpoint = fConnectionProperties.get(formattedPropertyName);
		if (TRACE.isLoggable(TraceLevel.INFO)) {
			TRACE.log(TraceLevel.INFO, "About to initialize object storage file system with endpoint '" + endpoint  + "'. Use configuration property '" + formattedPropertyName + "' to update it if required.");
		}
	    fFileSystem.initialize(new URI(fObjectStorageURI), fConnectionProperties);	
	    if (TRACE.isLoggable(TraceLevel.INFO)) {
	    	TRACE.log(TraceLevel.INFO, "Object storage client initialized with configuration: \n");
	    	for (Map.Entry<String, String> entry : fConnectionProperties) {
            	TRACE.log(TraceLevel.INFO, entry.getKey() + " = " + entry.getValue());
        	}
	    }
	}

	
	@Override
	public void initClientConfig() throws IOException, URISyntaxException {
		
		String protocol = Utils.getProtocol(fObjectStorageURI);

		// config authentication related properties
		OSAuthenticationHelper.configAuthProperties(protocol, fOpContext, fAppConfigCredentials, fConnectionProperties);
		
		fConnectionProperties.set(Constants.S3A_IMPL_CONFIG_NAME, Constants.S3A_DEFAULT_IMPL);
		//fConnectionProperties.set(Utils.formatProperty(Constants.S3A_SERVICE_ACCESS_KEY_CONFIG_NAME, protocol), fObjectStorageUser);
		//fConnectionProperties.set(Utils.formatProperty(Constants.S3A_SERVICE_SECRET_KEY_CONFIG_NAME, protocol), fObjectStoragePassword);			
		//fConnectionProperties.set(Utils.formatProperty(Constants.S3_ENDPOINT_CONFIG_NAME, protocol), Constants.S3_DEFAULT_ENDPOINT);
		fConnectionProperties.setIfUnset(Utils.formatProperty(Constants.S3_MULTIPART_CONFIG_NAME, protocol), Constants.S3_MULTIPATH_SIZE);
	
		fConnectionProperties.set(Constants.S3A_SIGNING_ALGORITHM_CONFIG_NAME, "S3SignerType");
		
		// Enable S3 path style access ie disabling the default virtual hosting behaviour.
		fConnectionProperties.set(Constants.S3A_PATH_STYLE_ACCESS_CONFIG_NAME, Boolean.TRUE.toString());			

		// -------------- Enable Streaming on output ----------------------------
		// Enable fast upload mechanism
		fConnectionProperties.set(Constants.S3A_FAST_UPLOAD_ENABLE_CONFIG_NAME, Boolean.TRUE.toString());
		
		// fs.s3a.fast.upload.buffer options: 
		// 1. "disk" will use the directories listed in fs.s3a.buffer.dir as
	    // 		  the location(s) to save data prior to being uploaded.
		// 2. "array" uses arrays in the JVM heap
		// 3. "bytebuffer" uses off-heap memory within the JVM.  
		// 
		// Both "array" and "bytebuffer" will consume memory in a single stream up to the number
	    // of blocks set by:

	    //    fs.s3a.multipart.size * fs.s3a.fast.upload.active.blocks.

	    //    If using either of these mechanisms, keep this value low

	    //    The total number of threads performing work across all threads is set by
	    //    fs.s3a.threads.max, with fs.s3a.max.total.tasks values setting the number of queued work items.
	    //fConnectionProperties.set(Constants.S3A_FAST_UPLOAD_BUFFER_CONFIG_NAME, "disk");
	    fConnectionProperties.set(Constants.S3A_FAST_UPLOAD_BUFFER_CONFIG_NAME, Constants.S3A_FAST_UPLOAD_DISK_BUFFER);
	    fConnectionProperties.set(Constants.S3A_MULTIPART_CONFIG_NAME, Constants.S3_MULTIPATH_SIZE);
	    fConnectionProperties.set(Constants.S3A_MAX_NUMBER_OF_ACTIVE_BLOCKS_CONFIG_NAME, String.valueOf(Constants.S3A_MAX_NUMBER_OF_ACTIVE_BLOCKS));
	    
	    
	    //fConnectionProperties.set(Constants.S3A_FAST_UPLOAD_BUFFER_CONFIG_NAME, "array");
	    if (fConnectionProperties.get(Constants.S3A_FAST_UPLOAD_BUFFER_CONFIG_NAME).equals(Constants.S3A_FAST_UPLOAD_DISK_BUFFER)) {
		    fConnectionProperties.set(Constants.S3A_DISK_BUFFER_DIR_CONFIG_NAME, Constants.S3A_DISK_BUFFER_ROOT_DIR + "/" + fOpContext.getPE().getPEId() + "-" + fOpContext.getName());			     
	    }
	}

	@Override
	public OutputStream getOutputStream(String filePath, boolean append)
			throws IOException {
		
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE,	"Get output stream for file path '" + filePath + "' in object storage with url '" + fObjectStorageURI + "'"); 
		}

		if (fIsDisconnected)
			return null;

		if (!append) {
			
			Path objPath = new Path(fObjectStorageURI, filePath);			
			boolean overwrite = true; // the current default behavior is
									  // to overwrite object if exists
			return fFileSystem.create(objPath, overwrite);
		} else {
			Path path = new Path(fObjectStorageURI, filePath);
			// if file exist, create output stream to append to file
			if (fFileSystem.exists(path)) {
				return fFileSystem.append(path);
			} else {
				OutputStream stream = fFileSystem.create(new Path(fObjectStorageURI, path));
				return stream;
			}
		}
	}

	public void cleanCacheIfRequired() {
		if (fConnectionProperties.get(Constants.S3A_FAST_UPLOAD_BUFFER_CONFIG_NAME).equals(Constants.S3A_FAST_UPLOAD_DISK_BUFFER)) {
			String diskCachePath = fConnectionProperties.get(Constants.S3A_DISK_BUFFER_DIR_CONFIG_NAME);
			try {
				FileUtils.forceDelete(new File(diskCachePath));
			} catch (IOException ioe) {
		    	if (TRACE.isLoggable(TraceLevel.WARNING)) {
					TRACE.log(TraceLevel.WARNING,"Failed to delete disk-cache '" + diskCachePath  + "'. Exception: '" + ioe.getMessage() + "'"); 
				}
			}
		}
	}
}
