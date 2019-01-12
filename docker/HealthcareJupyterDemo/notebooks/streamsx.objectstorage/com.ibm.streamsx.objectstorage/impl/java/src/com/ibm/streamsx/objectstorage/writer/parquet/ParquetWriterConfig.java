package com.ibm.streamsx.objectstorage.writer.parquet;

import org.apache.parquet.column.ParquetProperties.WriterVersion;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ParquetWriterConfig {
	
	private CompressionCodecName fCompressionType;
	private int fBlockSize;
	private int fPageSize;
	private int fDictPageSize;
	private boolean fEnableDictionary;
	private boolean fEnableSchemaValidation;
	private WriterVersion fParquetWriterVersion;
	
	public ParquetWriterConfig(CompressionCodecName compressionType,
				               int blockSize,
				               int pageSize,
				               int dictPageSize,
				               boolean enableDictionary,
				               boolean enableSchemaValidation,
				               WriterVersion parquetWriterVersion) {
		fCompressionType = compressionType;
		fBlockSize = blockSize;
		fPageSize = pageSize;
		fDictPageSize = dictPageSize;
		fEnableDictionary = enableDictionary;
		fEnableSchemaValidation = enableSchemaValidation;
		fParquetWriterVersion = parquetWriterVersion;
	}
	
	public CompressionCodecName getCompressionType() {
		return fCompressionType;
	}

	public int getBlockSize() {
		return fBlockSize;
	}

	public int getPageSize() {
		return fPageSize;
	}

	public int getDictPageSize() {
		return fDictPageSize;
	}


	public boolean isEnableSchemaValidation() {
		return fEnableSchemaValidation;
	}

	public WriterVersion getParquetWriterVersion() {
		return fParquetWriterVersion;
	}

	public boolean isEnableDictionary() {
		return fEnableDictionary;
	}

	
}
