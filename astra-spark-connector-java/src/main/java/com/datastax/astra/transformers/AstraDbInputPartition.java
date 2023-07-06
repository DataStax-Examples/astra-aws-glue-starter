package com.datastax.astra.transformers;

import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.sources.v2.reader.InputPartition;
import org.apache.spark.sql.sources.v2.reader.InputPartitionReader;

class AstraDbInputPartition implements InputPartition<InternalRow> {

    private int fileId;
    private String bucket, path;

    AstraDbInputPartition(int fileId, String bucket, String path) {
        this.fileId = fileId;
        this.bucket = bucket;
        this.path = path;
    }

    @Override
    public InputPartitionReader<InternalRow> createPartitionReader() {
        return new AstraDbInputPartitionReader(fileId, bucket, path);
    }
}
