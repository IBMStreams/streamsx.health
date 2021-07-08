/*******************************************************************************
* Copyright (C) 2014, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/

package com.ibm.streamsx.objectstorage.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;

public interface IObjectStorageClient {
	
	public void connect() throws Exception;
	
	public InputStream getInputStream(String objectPath) throws IOException;
	
	public OutputStream getOutputStream(String objectPath, boolean append) throws IOException;	
	
	public FileStatus[] scanDirectory(String dirPath, String filter) throws IOException;
	
	public boolean create(String path);
	
	public boolean exists(String path) throws IOException;
	
	public boolean isDirectory(String objectPath) throws IOException;
	
	public long getObjectSize(String objectname) throws IOException;
	
	public boolean rename(String src, String dst) throws IOException;
	
	public boolean delete(String objectPath, boolean recursive) throws IOException;
	
	public void disconnect() throws Exception;
	
	public void setConnectionProperty(String name, String value);
	
	public String getConnectionProperty(String name);
	
	public Configuration getConnectionConfiguration();
	
	public Map<String, String> getConnectionProperties();
	
	public String getObjectStorageURI();

	// the method shoudn't be necessarily
	// implemented by all clients - so keeping its
	// empty implementation as default
	public default void cleanCacheIfRequired() {}

}
