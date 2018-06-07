package com.score.cchain.config

import com.typesafe.config.ConfigFactory

trait ElasticConf {
  // config object
  val elasticConf = ConfigFactory.load("elastic.conf")

  // elastic config
  lazy val elasticCluster = elasticConf.getString("elastic.cluster")
  lazy val elasticIndexTransaction = elasticConf.getString("elastic.indexTransaction")
  lazy val elasticIndexBlocks = elasticConf.getString("elastic.indexBlocks")
  lazy val elasticHost = elasticConf.getString("elastic.host")
  lazy val elasticPort = elasticConf.getString("elastic.port").toInt
}
