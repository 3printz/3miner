package com.score.cchain.config

import com.typesafe.config.ConfigFactory

trait KafkaConf {
  // config object
  val conf = ConfigFactory.load("kafka.conf")

  // kafka config
  lazy val kafkaHost = conf.getString("kafka.host")
  lazy val kafkaPort = conf.getInt("kafka.port")
  lazy val kafkaTopic = conf.getString("kafka.topic")
}
