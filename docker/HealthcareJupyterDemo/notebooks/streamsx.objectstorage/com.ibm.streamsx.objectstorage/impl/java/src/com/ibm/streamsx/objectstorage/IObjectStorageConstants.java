/*******************************************************************************
* Copyright (C) 2014, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/

package com.ibm.streamsx.objectstorage;

public interface IObjectStorageConstants {
	
	
	/**
	 *  authentication related parameters
	 */
	// IAM
	public static final String PARAM_IAM_APIKEY = "IAMApiKey";
	public static final String PARAM_IAM_SERVICE_INSTANCE_ID = "IAMServiceInstanceId";
	public static final String PARAM_IAM_TOKEN_ENDPOINT = "IAMTokenEndpoint";	
	// Basic
	public static final String PARAM_OS_USER = "objectStorageUser";
	public static final String PARAM_OS_PASSWORD = "objectStoragePassword";
	public static final String PARAM_ACCESS_KEY_ID = "accessKeyID"; // s3 specific wrapper 
	public static final String PARAM_SECRET_ACCESS_KEY = "secretAccessKey"; // s3 specific wrapper
	public static final String PARAM_USER_ID = "userID"; // swift specific wrapper 
	public static final String PARAM_PASSWORD = "password"; // swift specific wrapper
	
	public static final String PARAM_APP_CONFIG_NAME = "appConfigName"; // application configuration name
    public static final String DEFAULT_COS_APP_CONFIG_NAME = "cos";
    public static final String DEFAULT_COS_CREDS_PROPERTY_NAME = "cos.creds";
	
	public static final String PARAM_OS_URI = "objectStorageURI";
	public static final String PARAM_OS_OBJECT_NAME = "objectName";
	public static final String PARAM_OS_OBJECT_PATH = "objectPath";
	public static final String PARAM_TIME_PER_OBJECT = "timePerObject";
	public static final String PARAM_CLOSE_ON_PUNCT = "closeOnPunct";
	public static final String PARAM_TUPLES_PER_OBJECT = "tuplesPerObject";
	public static final String PARAM_BYTES_PER_OBJECT = "bytesPerObject";
	public static final String PARAM_OBJECT_NAME_ATTR = "objectNameAttribute";
	public static final String PARAM_DATA_ATTR = "dataAttribute";
	public static final String PARAM_SLEEP_TIME = "sleepTime";
	public static final String PARAM_INITDELAY = "initDelay";
	public static final String PARAM_ENCODING = "encoding";
	public static final String PARAM_STORAGE_FORMAT = "storageFormat";
	public static final String OBJECT_VAR_PREFIX = "%";
	public static final String OBJECT_VAR_OBJECTNUM = "%OBJECTNUM";
	public static final String OBJECT_VAR_TIME = "%TIME";
	public static final String OBJECT_VAR_PELAUNCHNUM = "%PELAUNCHNUM";
	public static final String OBJECT_VAR_PEID = "%PEID";
	public static final String OBJECT_VAR_PROCID = "%PROCID";
	public static final String OBJECT_VAR_HOST = "%HOST";
	public static final String OBJECT_VAR_PARTITION = "%PARTITIONS";

	public final static String AUTH_PRINCIPAL = "authPrincipal";
	public final static String AUTH_KEYTAB = "authKeytab";
	public final static String CRED_FILE = "credFile";
	public static final String PARAM_PARQUET_COMPRESSION = "parquetCompression";
	public static final String PARAM_PARQUET_BLOCK_SIZE = "parquetBlockSize";
	public static final String PARAM_PARQUET_PAGE_SIZE = "parquetPageSize";
	public static final String PARAM_PARQUET_DICT_PAGE_SIZE = "parquetDictPageSize";
	public static final String PARAM_PARQUET_ENABLE_DICT = "parquetEnableDict";
	public static final String PARAM_PARQUET_ENABLE_SCHEMA_VALIDATION = "parquetEnableSchemaValidation";
	public static final String PARAM_PARQUET_WRITER_VERSION = "parquetWriterVersion";
	public static final String PARAM_PARTITION_VALUE_ATTRIBUTES = "partitionValueAttributes";
	public static final String PARAM_SKIP_PARTITION_ATTRS = "skipPartitionAttributes";
	public static final String PARAM_NULL_PARTITION_DEFAULT_VALUE = "nullPartitionDefaultValue";
	public static final String PARAM_UPLOAD_WORKERS_NUM = "uploadWorkersNum";
}
	
