package com.datastax.astra.transformers;

import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.sources.v2.DataSourceOptions;
import org.apache.spark.sql.sources.v2.writer.DataWriter;
import org.apache.spark.sql.sources.v2.writer.DataWriterFactory;

class AstraDbDataWriterFactory implements DataWriterFactory<InternalRow> {
    private String bucket, keyPrefix;

    public AstraDbDataWriterFactory(DataSourceOptions options) {
        bucket = options.get("bucket").get();
        keyPrefix = options.get("keyPrefix").get();
    }

    @Override
    public DataWriter<InternalRow> createDataWriter(int partitionId, long taskId, long epochId) {
        return new AstraDbDataWriter(partitionId, taskId, epochId, bucket, keyPrefix);
    }
}
