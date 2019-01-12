package com.ibm.streamsx.objectstorage.writer;

import static com.ibm.streamsx.objectstorage.Utils.getParamSingleBoolValue;
import static com.ibm.streamsx.objectstorage.Utils.getParamSingleIntValue;
import static com.ibm.streamsx.objectstorage.Utils.getParamSingleStringValue;

import org.apache.hadoop.fs.Path;
import java.util.logging.Logger;
import org.apache.parquet.column.ParquetProperties.WriterVersion;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.Type.MetaType;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.BaseObjectStorageSink;
import com.ibm.streamsx.objectstorage.IObjectStorageConstants;
import com.ibm.streamsx.objectstorage.client.IObjectStorageClient;
import com.ibm.streamsx.objectstorage.internal.sink.StorageFormat;
import com.ibm.streamsx.objectstorage.writer.parquet.ParquetOSWriter;
import com.ibm.streamsx.objectstorage.writer.parquet.ParquetWriterConfig;
import com.ibm.streamsx.objectstorage.writer.raw.RawAsyncWriter;
import com.ibm.streamsx.objectstorage.writer.raw.RawSyncWriter;

public class WriterFactory {

	private static final String CLASS_NAME = WriterFactory.class.getName();
	private static Logger TRACE = Logger.getLogger(CLASS_NAME);
	
	private static WriterFactory instance = null;

	
	public synchronized static WriterFactory getInstance() {
		if (instance == null) {
			instance = new WriterFactory();
		}

		return instance;
	}

	public IWriter getWriter(String path, 
			 OperatorContext opContext, 
			 int dataAttrIndex, 			 
			 IObjectStorageClient storageClient, 
			 StorageFormat fStorageFormat, 
			 byte[] newLine) throws Exception {
		
		IWriter res = null;
		
		boolean isBlob = dataAttrIndex >=0 ? com.ibm.streamsx.objectstorage.Utils.getAttrMetaType(opContext, dataAttrIndex) == MetaType.BLOB : false;
		
		switch (fStorageFormat) {
		case raw:
			if (TRACE.isLoggable(TraceLevel.TRACE)) {
				TRACE.log(TraceLevel.TRACE, "Creating raw sync writer for object with  path '" + path + "'");
			}

			//res = new RawAsyncWriter(path, opContext, storageClient, isBlob ? new byte[0] : newLine);
			res = new RawSyncWriter(path, opContext, storageClient, isBlob ? new byte[0] : newLine);
			break;

		case parquet:
			// container for default parquet options
			ParquetWriterConfig defaultParquetWriterConfig = ParquetOSWriter.getDefaultPWConfig();

			// initialize parquet related parameters (if exists) from the
			// context
			CompressionCodecName compressionType = CompressionCodecName
					.valueOf(getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_PARQUET_COMPRESSION,
							defaultParquetWriterConfig.getCompressionType().name()));

			int blockSize = getParamSingleIntValue(opContext, IObjectStorageConstants.PARAM_PARQUET_BLOCK_SIZE,
					defaultParquetWriterConfig.getBlockSize());
			int pageSize = getParamSingleIntValue(opContext, IObjectStorageConstants.PARAM_PARQUET_PAGE_SIZE,
					defaultParquetWriterConfig.getPageSize());
			int dictPageSize = getParamSingleIntValue(opContext, IObjectStorageConstants.PARAM_PARQUET_DICT_PAGE_SIZE,
					defaultParquetWriterConfig.getDictPageSize());
			boolean enableDictionary = getParamSingleBoolValue(opContext,
					IObjectStorageConstants.PARAM_PARQUET_ENABLE_DICT, defaultParquetWriterConfig.isEnableDictionary());
			boolean enableSchemaValidation = getParamSingleBoolValue(opContext,
					IObjectStorageConstants.PARAM_PARQUET_ENABLE_SCHEMA_VALIDATION,
					defaultParquetWriterConfig.isEnableSchemaValidation());
			WriterVersion parquetWriterVersion = WriterVersion.fromString(
					getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_PARQUET_WRITER_VERSION,
							defaultParquetWriterConfig.getParquetWriterVersion().name()));

			ParquetWriterConfig parquetWriterConfig = new ParquetWriterConfig(compressionType, blockSize, pageSize,
					dictPageSize, enableDictionary, enableSchemaValidation, parquetWriterVersion);

			if (TRACE.isLoggable(TraceLevel.TRACE)) {
				TRACE.log(TraceLevel.TRACE,
						"Creating parquet writer for object with parent path '"
								+ storageClient.getObjectStorageURI()
								+ "' and child path '" + path + "'");
			}

			res = new ParquetOSWriter(
					new Path(storageClient.getObjectStorageURI() + path),
					opContext,
					storageClient.getConnectionConfiguration(), parquetWriterConfig);

			break;

		default:
			if (TRACE.isLoggable(TraceLevel.TRACE)) {
				TRACE.log(TraceLevel.TRACE, "Creating raw async writer for object with  path '" + path + "'");
			}

			res = new RawAsyncWriter(path,
					opContext,
					storageClient,
					isBlob ? new byte[0] : newLine);
			break;
		}

		return res;
	}

}
