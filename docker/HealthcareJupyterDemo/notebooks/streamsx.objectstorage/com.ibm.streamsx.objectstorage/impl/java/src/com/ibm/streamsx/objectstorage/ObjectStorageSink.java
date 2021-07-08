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

@PrimitiveOperator(name="ObjectStorageSink", namespace="com.ibm.streamsx.objectstorage",
description=ObjectStorageSink.DESC+ObjectStorageSink.BASIC_DESC+AbstractObjectStorageOperator.AUTHENTICATION_DESC+ObjectStorageSink.STORAGE_FORMATS_DESC+ObjectStorageSink.ROLLING_POLICY_DESC)
@InputPorts({@InputPortSet(description="The `ObjectStorageSink` operator has one input port, which writes the contents of the input stream to the object that you specified. The `ObjectStorageSink` supports writing data into object storage in two formats `parquet` and `raw`. The storage format `raw` supports line format and blob format. For line format, the schema of the input port is tuple<rstring line>, which specifies a single rstring attribute that represents a line to be written to the object. For binary format, the schema of the input port is tuple<blob data>, which specifies a block of data to be written to the object.", cardinality=1, optional=false, windowingMode=WindowMode.NonWindowed, windowPunctuationInputMode=WindowPunctuationInputMode.Oblivious)})
@OutputPorts({@OutputPortSet(description="The `ObjectStorageSink` operator is configurable with an optional output port. The schema of the output port is <rstring objectName, uint64 objectSize>, which specifies the name and size of objects that are written to object storage. Note, that the tuple is generated on the object upload completion.", cardinality=1, optional=true, windowPunctuationOutputMode=WindowPunctuationOutputMode.Generating)})
@Libraries({"opt/*","opt/downloaded/*" })
public class ObjectStorageSink extends BaseObjectStorageSink implements IObjectStorageAuth {
	
	public static final String DESC = 
			"Operator writes objects to S3 compliant object storage. The operator supports basic (HMAC) and IAM authentication.";
	
	public static final String BASIC_DESC =
			"\\n"+
			"\\nThis operator writes tuples that arrive on its input port to the output object that is named by the **objectName** parameter. "+
			"You can optionally control whether the operator closes the current output object and creates a new object for writing based on the size"+ 
			"of the object in bytes, the number of tuples that are written to the object, or the time in seconds that the object is open for writing, "+
			"or when the operator receives a punctuation marker."+
			"\\n"+
			"\\n# Behavior in a consistent region\\n" +
			"\\nThe operator can participate in a consistent region. " +
			"The operator can be part of a consistent region, but cannot be at the start of a consistent region.\\n" +
			"The operator guarantees that tuples are written to a object in object storage at least once,\\n" +
			"but duplicated tuples can be written to the object if application failure occurs.\\n" +
			"\\nOn drain, the operator flushes its internal buffer and uploads the object to the object storage.\\n" +
			"On checkpoint, the operator stores the current object number to the checkpoint.\\n"+
			"\\nThe close mode can not be configured when running in a consistent region. The parameters `bytesPerObject`, `closeOnPunct`, `timePerObject` and `tuplesPerObject` are ignored.\\n"
		   	;

	public static final String STORAGE_FORMATS_DESC =
			"\\n"+
			"\\n+ Supported Storage Formats\\n"+ 
			"\\nThe operator support two storage formats:\\n"+
			"\\n* `parquet` - when output object is generated in parquet format\\n"+
			"\\n* `raw` - when output object is generated in the raw format\\n"+
			"\\nThe storage format can be configured with the `storageFormat` parameter.\\n"+
			"\\nThe `storageFormat` parameter supports two values: `parquet` and `raw`.\\n"+
			"\\n# Parquet Storage Format\\n"+
			"\\n"+
			"\\nParquet output schema is derived from the tuple structure. Note, that parquet format is supported "+
			"\\nfor tuples with the flat SPL schema only.\\n"+
			"\\n"+
			"\\nThe following table summarizes primitive SPL to Parquet types mapping:\\n"+
			"\\n"+
			"|:------------------------------------------|:------------:|\\n"+
			"| SPL Type                                  | Parquet Type |\\n"+
			"|:==========================================|:============:|\\n"+
			"| BOOLEAN                                   | boolean      |\\n"+
			"|-------------------------------------------|--------------|\\n"+
			"| INT8, UINT8, INT16, UINT16, INT32, UINT32 | int32        |\\n"+
			"|-------------------------------------------|--------------|\\n"+
			"| INT64, UINT64                             | int64        |\\n"+
			"|-------------------------------------------|--------------|\\n"+
			"| FLOAT32                                   | float        |\\n"+
			"|-------------------------------------------|--------------|\\n"+
			"| FLOAT64                                   | double       |\\n"+
			"|-------------------------------------------|--------------|\\n"+
			"| RSTRING, USTRING, BLOB                    | binary       |\\n"+
			"|-------------------------------------------|--------------|\\n"+
			"| TIMESTAMP                                 | int64        |\\n"+
			"|-------------------------------------------|--------------|\\n"+
			"| ALL OTHER SPL PRIMITIVE TYPES             | binary       |\\n"+
			"----------------------------------------------------------\\n"+
			"\\n"+
			"\\n"+
			"\\nThe following table summarizes collection SPL to Parquet types mapping:\\n"+
			"\\n"+
			"|:------------:|:---------------------------------------------------------------------:|\\n"+
			"| SPL Type     | Parquet Type                                                          |\\n"+
			"|:============:|:======================================================================|\\n"+
			"| LIST, SET    | optional group my_list (LIST) (repeated group of list/set elements)   |\\n"+
			"|--------------|-----------------------------------------------------------------------|\\n"+
			"| MAP          | repeated group of key/value                                           |\\n"+
			"---------------------------------------------------------------------------------------\\n"+
			"\\n"+			
			"\\n\\nParameters relevant for `parquet` storage format\\n"+
			"\\n* `nullPartitionDefaultValue` - Specifies default for partitions with null values.\\n"+
			"\\n* `parquetBlockSize` - Specifies the block size which is the size of a row group being buffered in memory. The default is 128M.\\n"+
			"\\n* `parquetCompression` - Enum specifying support compressions for parquet storage format. Supported compression types are 'UNCOMPRESSED','SNAPPY','GZIP'\\n"+
			"\\n* `parquetDictPageSize` - There is one dictionary page per column per row group when dictionary encoding is used. The dictionary page size works like the page size but for dictionary.\\n"+
			"\\n* `parquetEnableDict` - Specifies if parquet dictionary should be enabled.\\n"+
			"\\n* `parquetEnableSchemaValidation` - Specifies of schema validation should be enabled.\\n"+
			"\\n* `parquetPageSize` - Specifies the page size is for compression. A block is composed of pages. The page is the smallest unit that must be read fully to access a single record. If this value is too small, the compression will deteriorate. The default is 1M.\\n"+
			"\\n* `parquetWriterVersion` - Specifies parquet writer version. Supported versions are `1.0` and `2.0`\\n"+
			"\\n* `skipPartitionAttributes` - Avoids writing of attributes used as partition columns in data files.\\n"+
			"\\n* `partitionValueAttributes` - Specifies the list of attributes to be used for partition column values. Please note,"+ 
			"that its strongly recommended not to use attributes with continuous values per rolling policy unit of measure"+ 
			"to avoid operator performance degradation. "+
			"The following examples demonstrates recommended and non-recommended partitioning approaches."+
			"**Recommended**: /YEAR=YYYY/MONTH=MM/DAY=DD/HOUR=HH "+
			"**Non-recommended**: /latutide=DD.DDDD/longitude=DD.DDDD/\\n"+
			"\\n"+
			"\\n**Parquet storage format - preferred practices for partitions design**\\n"+
			"\\n1. Think about what kind of queries you will need. For example, you might need to build monthly reports or sales by product line.\\n"+
			"\\n2. Do not partition on an attribute with high cardinality per rolling policy window that you end up with too many simultaneously"+
			"active partitions. Reducing the number of  simultaneously active partitions can greatly improve performance and operator's resource consumption.\\n"+
			"\\n3. Do not partition on attribute with high cardinality per rolling policy window so you end up with many small-sized objects.\\n"+
			"\\n"+
			"\\n\\n# Raw Storage Format\\n"+
			"\\nIf the input tuple schema for the `raw` storage format has more than one input attribute the operators expect `dataAttribute` parameter "+
			"to be specified. The attribute specified as `dataAttribute` value should be of `rstring` or `blob` type.\\n"+
			"\\nParameters relevant for the `raw` storage format:\\n"+
			"\\n* `dataAttribute` - Required when input tuple has more than one attribute. Specifies the name of the attribute which "+
			"content is about to be written to the output object. The attribute should has `rstring` or `blob` SPL type.\\n"+
			"Mandatory parameter for the case when input tuple has more than one attribute and the storage format is set to `raw`.\\n"+
			"\\n* `objectNameAttribute` - If set, it points to the attribute containing an object name. The operator will close the object when value "+
			"of this attribute changes and will open the new object with an updated name.\\n"+
			"\\n* `encoding` - Specifies the character encoding that is used in the output object.\\n"+
			"\\n* `headerRow` - If specified the header line with the parameter content will be generated in each output object.\\n"			
		   	;
	
	
	public static final String ROLLING_POLICY_DESC =
			"\\n"+
			"\\n+ Rolling Policy\\n"+
			"\\nRolling policy specifies the window size managed by operator per output object."+ 
			"\\nWhen window is closed the current output object is closed and a new object is opened."+
			"\\nThe operator supports three rolling policy types:\\n"+
			"\\n* Size-based (parameter `bytesPerObject`)\\n"+
			"\\n* Time-based (parameter `timePerObject`)\\n"+
			"\\n* Tuple count-based (parameter `tuplesPerObject`)\\n"+
			"\\n"+
			"\\n# Object name\\n"+
			"\\nThe `objectName` parameter can optionally contain the following variables, which the operator evaluates at runtime "+
			"to generate the object name:\\n"+
			"\\n"+
			"\\n**%TIME** is the time when the COS object is created. The default time format is yyyyMMdd_HHmmss.\\n"+ 
			"\\n"+ 
			"\\nThe variable %TIME can be added anywhere in the path after the bucket name. The variable is typically used to "+ 
			"make dynamic object names when you expect the application to create multiple objects.\\n"+ 
			"\\nHere are some examples of valid file paths with %TIME:\\n"+
			"\\n * `event%TIME.parquet`\\n"+
			"\\n * `%TIME_event.parquet`\\n"+
			"\\n * `/my_new_folder/my_new_file_%TIME.csv`\\n"+
			"\\n"+ 			
			"\\n**%OBJECTNUM** is an object number, starting at 0, when a new object is created for writing.\\n"+ 
			"\\nObjects with the same name will be overwritten. Typically, %OBJECTNUM is added after the file name.\\n"+
			"\\nHere are some examples of valid file paths with %OBJECTNUM:\\n"+
			"\\n * `event_%OBJECTNUM.parquet`\\n"+
			"\\n * `/geo/uk/geo_%OBJECTNUM.parquet`\\n"+
			"\\n * `%OBJECTNUM_event.csv`\\n"+
			"\\n * `%OBJECTNUM_%TIME.csv`\\n"+
			"\\n"+ 
			"\\nNote: If partitioning is used, %OBJECTNUM is managed globally for all partitions in the COS object,"+ 
			"rather than independently for each partition."+
			"\\n"+ 
			"\\n**%PARTITIONS** place partitions anywhere in the object name.  By default, partitions are placed immediately before the last part of the object name.\\n"+
			"\\nHere's an example of default position of partitions in an object name: \\n"+
			"\\nSuppose that the file path is `/GeoData/test_%TIME.parquet`. Partitions are defined as YEAR, MONTH, DAY, and HOUR.\\n"+ 
			"\\nThe object in COS would be `/GeoData/YEAR=2014/MONTH=7/DAY=29/HOUR=36/test_20171022_124948.parquet` \\n"+
			"\\n"+ 
			"\\nWith %PARTITIONS, you can change the placement of partitions in the object name from the default. \\n"+
			"\\nLet's see how the partition placement changes by using %PARTITIONS:\\n"+
			"\\nSuppose that the file path now is `/GeoData/Asia/%PARTITIONS/test_%TIME.parquet.` \\n"+
			"\\nThe object name in COS would be `/GeoData/Asia/YEAR=2014/MONTH=7/DAY=29/HOUR=36/test_20171022_124948.parquet`\\n"+
			"\\n"+ 
			"\\n**Empty partition values** \\n"+
			"\\nIf a value in a partition is not valid, the invalid values are replaced by the string `__HIVE_DEFAULT_PARTITION__` in the COS object name.\\n"+ 
			"\\nFor example, `/GeoData/Asia/YEAR=2014/MONTH=7/DAY=29/HOUR=__HIVE_DEFAULT_PARTITION__/test_20171022_124948.parquet`\\n"+
			"\\n"+
			"\\n* `%HOST` the host that is running the processing element (PE) of this operator.\\n"+
			"\\n* `%PROCID` the process ID of the processing element running the this operator.\\n"+
			"\\n* `%PEID` the processing element ID.\\n"+
			"\\n* `%PELAUNCHNUM` the PE launch count.\\n"+
			"\\n"
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
		
	@Parameter(optional=false, description = "Specifies URI for connection to Cloud Object Storage (COS). For S3-compliant COS the URI should be in  'cos://bucket/ or s3a://bucket/' format. The bucket or container must exist. The operator does not create a bucket or container.")
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
