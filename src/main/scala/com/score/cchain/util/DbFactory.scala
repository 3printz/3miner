package com.score.cchain.util

import com.datastax.driver.core.{Cluster, HostDistance, PoolingOptions, Session}
import com.score.cchain.config.DbConf

object DbFactory extends DbConf {

  lazy val poolingOptions: PoolingOptions = {
    new PoolingOptions()
      .setConnectionsPerHost(HostDistance.LOCAL, 4, 10)
      .setConnectionsPerHost(HostDistance.REMOTE, 2, 4)
  }

  lazy val cluster: Cluster = {
    Cluster.builder()
      .addContactPoint(cassandraHost)
      .build()
  }

  lazy val session: Session = cluster.connect(cassandraKeyspace)

}
