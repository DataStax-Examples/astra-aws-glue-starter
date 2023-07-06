package com.datastax.astra;

import com.datastax.astra.transformers.AstraDbReader;
import com.datastax.astra.transformers.AstraDbWriter;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.sources.v2.DataSourceOptions;
import org.apache.spark.sql.sources.v2.DataSourceV2;
import org.apache.spark.sql.sources.v2.ReadSupport;
import org.apache.spark.sql.sources.v2.WriteSupport;
import org.apache.spark.sql.sources.v2.reader.DataSourceReader;
import org.apache.spark.sql.sources.v2.writer.DataSourceWriter;
import org.apache.spark.sql.types.StructType;

import java.util.Optional;

/**
 * AstraDbConnector is the entry point for the connector.
 *
 * - Read  from a Cassandra Table
 * - Write into a Cassandra Table
 */
public class AstraDbConnector implements DataSourceV2, ReadSupport, WriteSupport {

    @Override
    public DataSourceReader createReader(DataSourceOptions options) {
        return new AstraDbReader(options);
    }

    @Override
    public Optional<DataSourceWriter> createWriter(
            String writeUUID, StructType schema, SaveMode mode, DataSourceOptions options) {
        return Optional.of(new AstraDbWriter(options));
    }

}


