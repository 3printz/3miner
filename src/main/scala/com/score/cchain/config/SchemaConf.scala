package com.score.cchain.config

import com.typesafe.config.ConfigFactory

import scala.util.Try

trait SchemaConf {
  // config object
  val schemaConf = ConfigFactory.load("schema.conf")

  // cassandra config
  lazy val schemaCreateKeyspace = Try(schemaConf.getString("schema.createKeyspace")).getOrElse("")
  lazy val schemaCreateTypePromize = Try(schemaConf.getString("schema.createTypePromize")).getOrElse("")
  lazy val schemaCreateTypeTransaction = Try(schemaConf.getString("schema.createTypeTransaction")).getOrElse("")
  lazy val schemaCreateTypeSignature = Try(schemaConf.getString("schema.createTypeSignature")).getOrElse("")
  lazy val schemaCreateTablePromizes = Try(schemaConf.getString("schema.createTablePromizes")).getOrElse("")
  lazy val schemaCreateTableTrans = Try(schemaConf.getString("schema.createTableTrans")).getOrElse("")
  lazy val schemaCreateTableTransactions = Try(schemaConf.getString("schema.createTableTransactions")).getOrElse("")
  lazy val schemaCreateTableBlocks = Try(schemaConf.getString("schema.createTableBlocks")).getOrElse("")
  lazy val schemaCreateTableHashes = Try(schemaConf.getString("schema.createTableHashes")).getOrElse("")
  lazy val schemaCreateTableUsers = Try(schemaConf.getString("schema.createTableUsers")).getOrElse("")
  lazy val schemaCreateFromIndex = Try(schemaConf.getString("schema.createFromIndex")).getOrElse("")
  lazy val schemaCreateToIndex = Try(schemaConf.getString("schema.createToIndex")).getOrElse("")
  lazy val schemaCreatePromizeIndex = Try(schemaConf.getString("schema.createPromizeIndex")).getOrElse("")
  lazy val schemaCreateTransactionLuceneIndex = Try(schemaConf.getString("schema.createTransactionLuceneIndex")).getOrElse("")
  lazy val schemaCreatePromizeLuceneIndex = Try(schemaConf.getString("schema.createPromizeLuceneIndex")).getOrElse("")
  lazy val schemaCreateBlockLuceneIndex = Try(schemaConf.getString("schema.createBlockLuceneIndex")).getOrElse("")
}
