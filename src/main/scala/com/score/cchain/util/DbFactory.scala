package com.score.cchain.util

import com.datastax.driver.core.{Cluster, HostDistance, PoolingOptions, Session}
import com.score.cchain.config.{DbConf, SchemaConf}

object DbFactory extends DbConf with SchemaConf {

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

  val initDb = () => {
    // TODO we disabled this
    // session.execute(schemaCreateKeyspace)

    // create UDT
    session.execute(schemaCreateTypePromize)
    session.execute(schemaCreateTypeTransaction)
    session.execute(schemaCreateTypeSignature)

    // create tables
    session.execute(schemaCreateTablePromizes)
    session.execute(schemaCreateTableTrans)
    session.execute(schemaCreateTableTransactions)
    session.execute(schemaCreateTableBlocks)
    session.execute(schemaCreateTableHashes)
    session.execute(schemaCreateTableUsers)

    // create index
    session.execute(schemaCreateFromIndex)
    session.execute(schemaCreateToIndex)
    session.execute(schemaCreatePromizeIndex)
    session.execute(schemaCreateTransactionLuceneIndex)
    session.execute(schemaCreatePromizeLuceneIndex)
    session.execute(schemaCreateBlockLuceneIndex)
  }

}
