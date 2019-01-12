/*******************************************************************************
* Copyright (C) 2014, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/
package com.ibm.streamsx.objectstorage.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.Utils;


public abstract class ObjectStorageAbstractClient implements IObjectStorageClient {
	
	protected FileSystem fFileSystem;
	protected boolean fIsDisconnected;
	protected Configuration fConnectionProperties = new Configuration();
	protected String fObjectStorageURI;
	protected String fEndpoint;
	protected OperatorContext fOpContext;
	protected Properties fAppConfigCredentials;
	
	private static Logger TRACE = Logger.getLogger(ObjectStorageAbstractClient.class.getName());


	public ObjectStorageAbstractClient(String objectStorageURI,
			                           OperatorContext opContext,
			                           Properties appConfigCreds) throws Exception {
		fObjectStorageURI = objectStorageURI;
		fOpContext = opContext;
		fAppConfigCredentials = appConfigCreds;
		
		/**
		 * initialize configuration
		 */
		initClientConfig();
		fConnectionProperties.set(Constants.SUPPORTED_SCHEME_LIST_CONFIG_NAME, Arrays.toString(Constants.SUPPORTED_SCHEME_LIST).replaceAll("\\[|\\]", ""));
	}

	public ObjectStorageAbstractClient(String objectStorageURI,
			                   		   OperatorContext opContext,
			                   		   Properties appConfigCreds,
							           Configuration config) throws Exception {
		
		this(objectStorageURI, opContext, appConfigCreds);
		
		/**
		 * Update default configuration and add uninitialized properties
		 */
		for (Map.Entry<String, String> entry : config) {
			fConnectionProperties.set(entry.getKey(), entry.getValue());
        }
	}

	
	/**
	 * Client specific connection configuration settings
	 * @return
	 * @throws Exception 
	 */
	public abstract void initClientConfig() throws Exception;
	
	@Override
	public void connect() throws Exception {
		initClientConfig();
	    fFileSystem = new com.ibm.stocator.fs.ObjectStoreFileSystem();	
		String formattedPropertyName = Utils.formatProperty(Constants.S3_SERVICE_ENDPOINT_CONFIG_NAME, Utils.getProtocol(fObjectStorageURI));
		fEndpoint = fConnectionProperties.get(formattedPropertyName);
		if (TRACE.isLoggable(TraceLevel.INFO)) {
			TRACE.log(TraceLevel.INFO, "About to initialize object storage file system with endpoint '" + fEndpoint  + "'. Use configuration property '" + formattedPropertyName + "' to update it if required.");
		}
	    fFileSystem.initialize(Utils.getEncodedURI(fObjectStorageURI), fConnectionProperties);				
	    if (TRACE.isLoggable(TraceLevel.INFO)) {
	    	TRACE.log(TraceLevel.INFO, "Object storage client initialized with configuration: \n");
	    	for (Map.Entry<String, String> entry : fConnectionProperties) {
	    		TRACE.log(TraceLevel.INFO, entry.getKey() + " = " + entry.getValue());
	    	}
	    }
	}
	
	@Override
	public boolean create(String name)  {
		try {
			OutputStream outStream = getOutputStream(name, false);	
			outStream.close();
			return true;
		} catch (Exception e) {	
			TRACE.log(TraceLevel.ERROR,	"Failed to create path '" + name + "'. ERROR: '" + e.getMessage() + "'");
			return false;		
		}
	}
	
	
	
	@Override
	public InputStream getInputStream(String filePath) throws IOException {
		if (fIsDisconnected) {
			return null;
		}		
		return fFileSystem.open(new Path(fObjectStorageURI, filePath));
	}

	@Override
	public synchronized OutputStream getOutputStream(String filePath, boolean append)
			throws IOException {
		
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE,	"Get output stream for file path '" + filePath + "' in object storage with url '" + fObjectStorageURI + "'"); 
		}

		if (fIsDisconnected)
			return null;

		if (!append) {			
			return fFileSystem.create(new Path(fObjectStorageURI, filePath), true);
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
	
	
	@Override
	public boolean exists(String filePath) throws IOException {

		if (fIsDisconnected)
			return true;

		return fFileSystem.exists(new Path(fObjectStorageURI, filePath));
	}

	@Override
	public long getObjectSize(String filename) throws IOException {

		if (fIsDisconnected)
			return 0;

		FileStatus fileStatus = fFileSystem.getFileStatus(new Path(fObjectStorageURI, filename));
		return fileStatus.getLen();
	}

	@Override
	public boolean rename(String src, String dst) throws IOException {

		if (fIsDisconnected)
			return false;

		Path srcPath = new Path(fObjectStorageURI, src);
		Path dstPath = new Path(fObjectStorageURI, dst);
		Path parentPath = dstPath.getParent();
		if (parentPath != null) {
			if ( ! fFileSystem.mkdirs(parentPath)) {
				return false;
			}
		}
		return fFileSystem.rename(srcPath, dstPath);
	}

	@Override
	public boolean delete(String filePath, boolean recursive) throws IOException {
		if (fIsDisconnected)
			return false;
		
		Path f = new Path(fObjectStorageURI, filePath);
		return fFileSystem.delete(f, recursive);
	}

	@Override
	public boolean isDirectory(String filePath) throws IOException {

		if (fIsDisconnected)
			return false;

		return fFileSystem.getFileStatus(new Path(fObjectStorageURI, filePath)).isDirectory();
	}
	
	@Override
	public void disconnect() throws Exception {
		fFileSystem.close();
		fIsDisconnected = true;		
	}
	
	@Override
	public void setConnectionProperty(String name, String value) {
		fConnectionProperties.set(name, value);
	}
	
	@Override
	public String getConnectionProperty(String name) {
		return fConnectionProperties.get(name);
	}
	
	

	
	@Override
	public Map<String, String> getConnectionProperties() {
		HashMap<String, String> res = new HashMap<String, String>();
		Iterator<Entry<String, String>>  propsIterator = fConnectionProperties.iterator();
		while (propsIterator.hasNext()) {
			Entry<String, String> curr = propsIterator.next();
			res.put(curr.getKey(), curr.getValue());
		}
		
		return res;
	}
	
	@Override
	public Configuration getConnectionConfiguration() {
		return fConnectionProperties;
	}
	
	public FileSystem getFileSystem() {
		return fFileSystem;
	}
	
	public FileStatus[] scanDirectory(String dirPath, String filter)
			throws IOException {

		FileStatus[] files = new FileStatus[0];
		ArrayList<FileStatus> matchFileStatuses = new ArrayList<FileStatus>();

		Path path = new Path(fObjectStorageURI, dirPath);

		if (fFileSystem.exists(path)) {
			files = fFileSystem.listStatus(new Path(fObjectStorageURI, dirPath));
			if (filter != null && !filter.isEmpty()) {
				// @TODO: currently path filter is not supported by stocator - implementing
				// it locally 
				files = fFileSystem.listStatus(new Path(fObjectStorageURI, dirPath));
				String filePath = "";
				for (FileStatus fstatus: files) {
					filePath = fstatus.getPath().getName();
					if (TRACE.isLoggable(TraceLevel.INFO)) {
						TRACE.log(TraceLevel.INFO,	"Checking file path '" + filePath + "' match with filter '" + filter + "'..."); 
					}
					if (filePath.matches(filter)) {
						matchFileStatuses.add(fstatus);
						if (TRACE.isLoggable(TraceLevel.INFO)) {
							TRACE.log(TraceLevel.INFO,	"File path '" + filePath + "' matches filter '" + filter + "'"); 
						}
					}
					else {
						if (TRACE.isLoggable(TraceLevel.INFO)) {
							TRACE.log(TraceLevel.INFO,	"File path '" + filePath + "' does not match filter '" + filter + "'. Skipping ..."); 
						}
						
					}
				}
			}
		}
		return matchFileStatuses.toArray(new FileStatus[matchFileStatuses.size()]);
	}
	
	public String getObjectStorageURI() {
		return fObjectStorageURI;
	}
}
