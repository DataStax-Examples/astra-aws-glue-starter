package com.datastax.astra

/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.spark.sql.connector.catalog.{Table, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util
import scala.collection.JavaConverters._

/** Entry point of the connector.
 * Since Spark 3, 'DefaultSource' class is called when access to a source if you pass the package name to DataFrameReader.
 * In this connection, the entry point class is changed by the DataSourceRegister definition.
 *
 * (e.g.) val df = spark.read.format("tpcds").option(...)
 */
class TPCDSSource extends TableProvider with DataSourceRegister {
  // The requested table schema is fixed in this connector.
  // Then, a specific schema come from user options will be passed here.
  override def inferSchema(caseInsensitiveStringMap: CaseInsensitiveStringMap): StructType = null

  override def getTable(structType: StructType, transforms: Array[Transform], javaProps: util.Map[String, String]): Table = {
    require(javaProps.asScala.contains("table"), "You need to specify 'table' parameter.")
    val scale: Int = javaProps.asScala.getOrElse("scale", 1).toString.toInt
    val numPartitions: Int = javaProps.asScala.getOrElse("numPartitions", 1).toString.toInt
    val tableName: String = javaProps.asScala("table")

    new TPCDSTable(scale, tableName, numPartitions)
  }

  override def shortName(): String = "tpcds"
}
