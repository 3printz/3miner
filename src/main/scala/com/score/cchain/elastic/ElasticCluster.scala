package com.score.cchain.elastic

import java.net.InetSocketAddress

import com.score.cchain.config.ElasticConf
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient

trait ElasticCluster extends ElasticConf {
  lazy val settings = Settings.builder().put("cluster.name", elasticCluster).build()
  val client = new PreBuiltTransportClient(settings)
  client.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(elasticHost, elasticPort)))
}
