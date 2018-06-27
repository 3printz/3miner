package com.score.cchain.db

import com.datastax.driver.core._
import com.score.cchain.config.CassandraConf

/**
  * Cassandra database related configuration, we wrapped them with
  * trait in order to have self typed annotated dependencies
  *
  * @author eranga bandara(eranga.herath@pagero.com)
  */
trait CassandraCluster extends CassandraConf {
  lazy val poolingOptions: PoolingOptions = {
    new PoolingOptions()
      .setConnectionsPerHost(HostDistance.LOCAL, 4, 10)
      .setConnectionsPerHost(HostDistance.REMOTE, 2, 4)
  }
  lazy val cluster: Cluster = {
    val builder = Cluster.builder()
    builder.addContactPoint(cassandraHost)
    builder.withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM))
    builder.withPort(cassandraPort)
    //builder.withPoolingOptions(poolingOptions)

    builder.build()
  }
  lazy val session: Session = cluster.connect()

  // UDTs (transaction, signature)
  lazy val transType = cluster.getMetadata.getKeyspace(cassandraKeyspace).getUserType("transaction")
  lazy val sigType = cluster.getMetadata.getKeyspace(cassandraKeyspace).getUserType("signature")
}
