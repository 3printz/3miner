package com.score.cchain.config

import com.typesafe.config.ConfigFactory

import scala.util.Try

/**
  * Load db configurations define in cassandra.conf from here
  *
  * @author eranga herath(erangaeb@gmail.com)
  */
trait CassandraConf {
  // config object
  val dbConf = ConfigFactory.load("cassandra.conf")

  // cassandra config
  lazy val cassandraKeyspace = Try(dbConf.getString("db.cassandra.keyspace")).getOrElse("zchain")
  lazy val cassandraHost = Try(dbConf.getString("db.cassandra.host")).getOrElse("dev.localhost")
  lazy val cassandraPort = Try(dbConf.getInt("db.cassandra.port")).getOrElse(9042)
}
