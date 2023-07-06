package com.datastax.astra.transformers;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.sources.v2.writer.DataWriter;
import org.apache.spark.sql.sources.v2.writer.WriterCommitMessage;

import java.io.IOException;

class AstraDbDataWriter implements DataWriter<InternalRow> {

    public AstraDbDataWriter(int partitionId, long taskId, long epochId, String bucket, String keyPrefix) {
        // Initialization of the connection
    }

    @Override
    public void write(InternalRow record) throws IOException {
        // Adding a Statement to the batch for the current partition

    }

    @Override
    public WriterCommitMessage commit() throws IOException {
        // execute the statement
        return null;
    }

    @Override
    public void abort() throws IOException {
    }
}
