package com.datastax.astra

import org.apache.spark.sql.connector.catalog.{SupportsRead, SupportsWrite, Table, TableCapability}
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.connector.write.{LogicalWriteInfo, WriteBuilder}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util

class AstraDbTable extends Table with SupportsRead with SupportsWrite {
  override def name(): String = this.getClass.toString // Table name from specified option

  override def schema(): StructType =
    new StructType()
      .add("id", "int")
      .add("value", "int")

  import scala.collection.JavaConverters._

  override def capabilities(): util.Set[TableCapability] = Set(
    TableCapability.BATCH_READ,
    TableCapability.BATCH_WRITE).asJava; // Supports BATCH read/write mode

  // Read
  override def newScanBuilder(caseInsensitiveStringMap: CaseInsensitiveStringMap): ScanBuilder =
    new SimpleScanBuilder(this.schema())

  // Write
  override def newWriteBuilder(logicalWriteInfo: LogicalWriteInfo): WriteBuilder =
    new SimpleWriteBuilder
}
