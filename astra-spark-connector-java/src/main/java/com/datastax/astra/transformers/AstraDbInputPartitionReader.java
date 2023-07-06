package com.datastax.astra.transformers;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.sources.v2.reader.InputPartitionReader;
import org.apache.spark.unsafe.types.UTF8String;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class AstraDbInputPartitionReader implements InputPartitionReader<InternalRow> {
    BufferedReader reader;
    String line = null;

    AstraDbInputPartitionReader(int filedId, String bucket, String path) {
        AmazonS3 s3Client = AmazonS3Client.builder()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(new DefaultAwsRegionProviderChain().getRegion())
                .withForceGlobalBucketAccessEnabled(true)
                .build();
        S3Object s3Object = s3Client
                .getObject(new GetObjectRequest(bucket, String.format("%s%s.csv", path, filedId)));
        reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
    }

    @Override
    public boolean next() {
        try {
            line = reader.readLine();
            return line != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InternalRow get() {
        String[] fields = line.split(",");
        return new GenericInternalRow(new Object[]{UTF8String.fromString(fields[0]), Integer.parseInt(fields[1])});
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }
}
