package com.score.cchain.config

import com.typesafe.config.ConfigFactory

import scala.util.Try

trait SchemaConf {
  // config object
  val schemaConf = ConfigFactory.load("schema.conf")

  // cassandra config
  lazy val schemaCreateKeyspace = Try(schemaConf.getString("schema.createKeyspace")).getOrElse("")
  lazy val schemaCreateTypeTransaction = Try(schemaConf.getString("schema.createTypeTransaction")).getOrElse("")
  lazy val schemaCreateTypeSignature = Try(schemaConf.getString("schema.createTypeSignature")).getOrElse("")
  lazy val schemaCreateTableTrans = Try(schemaConf.getString("schema.createTableTrans")).getOrElse("")
  lazy val schemaCreateTableTransactions = Try(schemaConf.getString("schema.createTableTransactions")).getOrElse("")
  lazy val schemaCreateTableBlocks = Try(schemaConf.getString("schema.createTableBlocks")).getOrElse("")
  lazy val schemaCreateTableHashes = Try(schemaConf.getString("schema.createTableHashes")).getOrElse("")
  lazy val schemaCreateTableSenzies = Try(schemaConf.getString("schema.createTableSenzies")).getOrElse("")
  lazy val schemaCreateFromIndex = Try(schemaConf.getString("schema.createFromIndex")).getOrElse("")
  lazy val schemaCreateToIndex = Try(schemaConf.getString("schema.createToIndex")).getOrElse("")
}
