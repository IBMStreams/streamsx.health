package com.ibm.streamsx.objectstorage.writer.parquet;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.schema.MessageTypeParser;

public class ParquetHadoopWriter {
			
	public static ParquetWriter<List<String>> build(final Path outFilePath, String parquetSchema, final ParquetWriterConfig pwConfig, final Configuration config) throws IOException {				
		
		return new ParquetHadoopWriter.Builder(outFilePath, parquetSchema).
					  withWriteMode(ParquetFileWriter.Mode.OVERWRITE).
					  withCompressionCodec(pwConfig.getCompressionType()).
					  withRowGroupSize(pwConfig.getBlockSize()).
					  withDictionaryPageSize((pwConfig.getDictPageSize())).
					  withPageSize(pwConfig.getPageSize()).
					  enableDictionaryEncoding(pwConfig.isEnableDictionary()).
					  enableSchemaValidation(pwConfig.isEnableSchemaValidation()).
					  withWriterVersion(pwConfig.getParquetWriterVersion()).
					  withConf(config).
					  build();
	}

	public static class Builder extends ParquetWriter.Builder<List<String>, Builder> {

		private String fParquetSchema = null;
		
		protected Builder(Path file, String parquetSchema) {
			super(file);
			fParquetSchema = parquetSchema;
		}

		@Override
		protected Builder self() {			
			return this;
		}
		
		protected Builder enableDictionaryEncoding(boolean encoding) {
			if (encoding) 
				return this.enableDictionaryEncoding();
			return this;
		}

		protected Builder enableSchemaValidation(boolean validation) {
			if (validation) 
				return this.enableValidation();
			return this;
		}

		@Override
		protected WriteSupport<List<String>> getWriteSupport(Configuration paramConfiguration) {
			WriteSupport<List<String>> StringListWritableSupport = new StringListWriteSupport(
					MessageTypeParser.parseMessageType(fParquetSchema));
			
			return StringListWritableSupport;
		}
		
	}
}