package com.ibm.streamsx.objectstorage.writer.parquet;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.column.ColumnDescriptor;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.io.ParquetEncodingException;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.RecordConsumer;
import org.apache.parquet.schema.MessageType;

public class StringListWriteSupport extends WriteSupport<List<String>> {
  MessageType schema;
  RecordConsumer recordConsumer;
  List<ColumnDescriptor> cols;

  // TODO: support specifying encodings and compression
  public StringListWriteSupport(MessageType schema) {
    this.schema = schema;
    this.cols = schema.getColumns();
  }

  @Override
  public WriteContext init(Configuration configuration) {
    return new WriteContext(schema, new HashMap<String, String>());
    // TODO: Do we need something like this for configuration or schema-type...?
//	public WriteContext init(Configuration configuration) {
//		configuration.set(DataWritableWriteSupport.PARQUET_HIVE_SCHEMA, fParquetSchema);
//		return super.init(configuration);
//	}
  }

  @Override
  public void prepareForWrite(RecordConsumer r) {
    recordConsumer = r;
  }

  @Override
  public void write(List<String> values) {
    if (values.size() != cols.size()) {
      throw new ParquetEncodingException("Invalid input data. Expecting " +
          cols.size() + " columns. Input had " + values.size() + " columns (" + cols + ") : " + values);
    }

    recordConsumer.startMessage();
    for (int i = 0; i < cols.size(); ++i) {
      String val = values.get(i);
      // val.length() == 0 indicates a NULL value.
      if (val.length() > 0) {
        recordConsumer.startField(cols.get(i).getPath()[0], i);
        switch (cols.get(i).getType()) {
        case BOOLEAN:
          recordConsumer.addBoolean(Boolean.parseBoolean(val));
          break;
        case FLOAT:
          recordConsumer.addFloat(Float.parseFloat(val));
          break;
        case DOUBLE:
          recordConsumer.addDouble(Double.parseDouble(val));
          break;
        case INT32:
          recordConsumer.addInteger(Integer.parseInt(val));
          break;
        case INT64:
          recordConsumer.addLong(Long.parseLong(val));
          break;
        case INT96:
			Timestamp ts = Timestamp.valueOf(val);
            recordConsumer.addBinary(ParquetTimestamp.getBinary(ts, true));
          break;
        case BINARY:
          recordConsumer.addBinary(stringToBinary(val));
          break;
        default:
          throw new ParquetEncodingException(
              cols.get(i).getPath()[0] + ": " +
              "Unsupported column type: " + cols.get(i).getType() +
              " (" + val + ")");
        }
        recordConsumer.endField(cols.get(i).getPath()[0], i);
      }
    }
    recordConsumer.endMessage();
  }

  private Binary stringToBinary(Object value) {
    return Binary.fromString(value.toString());
  }
}