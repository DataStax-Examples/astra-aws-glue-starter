package com.datastax.astra.glue

import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.catalog.{Table, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.connector.read._
import org.apache.spark.sql.connector.write._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.{SparkConf, SparkContext}

import java.io.IOException
import java.util

class AstraDbSource extends TableProvider {

  val conf = new SparkConf(true)
      .set("spark.cassandra.auth.username", "token")
      .set("spark.cassandra.auth.password", "AstraCS:...")
      .set("spark.cassandra.connection.config.cloud.path", "/Downloads/secure-connect-demo.zip")
      .set("spark.dse.continuousPagingEnabled", "false")
      .set("spark.files", "$SECURE_CONNECT_BUNDLE_FILE_PATH/secure-connect-{{safeName}}.zip")

   val sc = new SparkContext("spark://glue:7077", "example", conf)

    sc.cassandraTable("keyspace", "table")
    //sc.read.cassandraFormat("tables", "system_schema").load().count()


  //val df = sc.read.cassandraFormat("kv", "ks").load()
  // Create keyspace and table
  CassandraConnector(sc).withSessionDo { session =>
    session.execute(
      """CREATE KEYSPACE IF NOT EXISTS ks WITH
        | replication = {'class': 'SimpleStrategy', 'replication_factor': 1 }""".stripMargin)
    session.execute("""CREATE TABLE IF NOT EXISTS ks.kv (k int, v int, PRIMARY KEY (k))""")
  }

  val rdd = sc.cassandraTable(keyspace = "ks", table = "kv")

  override def inferSchema(caseInsensitiveStringMap: CaseInsensitiveStringMap): StructType =
    getTable(
      null,
      Array.empty[Transform],
      caseInsensitiveStringMap.asCaseSensitiveMap()).schema()

  override def getTable(structType: StructType, transforms: Array[Transform], javaProps: util.Map[String, String]): Table = {
    // If you handle special options passed from connection options,
    // you can do it through processing `javaProps`.
    new AstraDbTable
  }
}



/* Read */
class SimpleScanBuilder(schema: StructType) extends ScanBuilder {
  override def build(): Scan = new SimpleScan(schema)
}

class SimpleScan(schema: StructType) extends Scan {
  override def readSchema(): StructType = schema

  override def toBatch: Batch = new SimpleBatch()
}

class SimpleBatch extends Batch {
  override def planInputPartitions(): Array[InputPartition] = {
    Array(
      new SimpleInputPartition(0, 5),
      new SimpleInputPartition(5, 10)
    )
  }

  override def createReaderFactory(): PartitionReaderFactory = new SimpleReaderFactory
}

class SimpleInputPartition(var start: Int, var end: Int) extends InputPartition {
  override def preferredLocations(): Array[String] = super.preferredLocations()
}

class SimpleReaderFactory extends PartitionReaderFactory {
  override def createReader(inputPartition: InputPartition): PartitionReader[InternalRow] =
    new SimplePartitionReader(inputPartition.asInstanceOf[SimpleInputPartition])
}

class SimplePartitionReader(val simpleInputPartition: SimpleInputPartition) extends PartitionReader[InternalRow] {
  var start: Int = simpleInputPartition.start
  var end: Int = simpleInputPartition.end
  override def next(): Boolean = {
    start = start + 1
    start < end
  }

  override def get(): InternalRow = {
    val row = Array(start, -start)
    InternalRow.fromSeq(row.toSeq)
  }

  @throws[IOException]
  override def close(): Unit = {}
}

/* Write */
class SimpleWriteBuilder extends WriteBuilder {
  override def buildForBatch(): BatchWrite =
    new SimpleBatchWrite
}

class SimpleBatchWrite extends BatchWrite {
  override def abort(writerCommitMessages: Array[WriterCommitMessage]): Unit = {}
  override def commit(writerCommitMessages: Array[WriterCommitMessage]): Unit = {}
  override def createBatchWriterFactory(physicalWriteInfo: PhysicalWriteInfo): DataWriterFactory =
    new SimpleDataWriterFactory
}

class SimpleDataWriterFactory extends DataWriterFactory {
  override def createWriter(partitionId: Int, taskId: Long): DataWriter[InternalRow] =
    new SimpleDataWriter(partitionId, taskId)
}

class SimpleDataWriter(partitionId: Int, taskId: Long) extends DataWriter[InternalRow] {
  @throws[IOException]
  override def abort(): Unit = {}

  @throws[IOException]
  override def commit(): WriterCommitMessage = null

  @throws[IOException]
  override def write(record: InternalRow): Unit = {
    // In this sample code, this part simply prints records for testing-purpose.
    println(s"write a record with id : ${record.getInt(0)} and value: ${record.getInt(1)} for partitionId: $partitionId by taskId: $taskId")
  }

  @throws[IOException]
  override def close(): Unit = {}
}