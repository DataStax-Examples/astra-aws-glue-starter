package com.datastax.astra.transformers;

import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.sources.v2.DataSourceOptions;
import org.apache.spark.sql.sources.v2.reader.DataSourceReader;
import org.apache.spark.sql.sources.v2.reader.InputPartition;
import org.apache.spark.sql.types.StructType;

import java.util.List;

public class AstraDbReader implements DataSourceReader {
    // Assuming the csv files to be read has a fixed schema on premise with two integer columns name, id.
    private final StructType schema = new StructType()
            .add("name", "string")
            .add("id", "int");
    private String bucket, path;

    public AstraDbReader(DataSourceOptions options) {
        bucket = options.get("bucket").get();
        path = options.get("path").get();
    }


    @Override
    public StructType readSchema() {
        return schema;
    }

    @Override
    public List<InputPartition<InternalRow>> planInputPartitions() {
        // parallel reading with simple partition planning - each partition read a specific csv file.
        return java.util.Arrays.asList(
                new AstraDbInputPartition(1, bucket, path),
                new AstraDbInputPartition(2, bucket, path));
    }
}
