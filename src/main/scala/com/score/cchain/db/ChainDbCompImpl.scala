package com.score.cchain.db

import java.util.UUID

import com.datastax.driver.core.UDTValue
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.score.cchain.config.CassandraConf
import com.score.cchain.protocol._
import com.score.cchain.util.MinerFactory

import scala.collection.JavaConverters._

trait ChainDbCompImpl extends ChainDbComp with CassandraConf {

  val chainDb = new ChainDbImpl

  class ChainDbImpl extends ChainDb {

    def getTrans: List[Trans] = {
      // select query
      val selectStmt = select()
        .all()
        .from(cassandraKeyspace, "trans")

      // get all trans
      val resultSet = MinerFactory.session.execute(selectStmt)
      resultSet.all().asScala.map { row =>
        Trans(row.getString("origin_zaddress"),
          row.getUUID("id"),
          row.getString("from_zaddress"),
          row.getString("to_zaddress"),
          row.getString("action"),
          row.getString("blob"),
          row.getLong("timestamp"),
          row.getString("digsig")
        )
      }.toList
    }

    def deleteTrans(trans: List[Trans]): Unit = {
      for (t <- trans) {
        // delete query
        val delStmt = delete()
          .from(cassandraKeyspace, "trans")
          .where(QueryBuilder.eq("origin_zaddress", t.oriZaddr)).and(QueryBuilder.eq("id", t.id))

        MinerFactory.session.execute(delStmt)
      }
    }

    def createPreHash(hash: String): Unit = {
      // insert query
      val statement = QueryBuilder.insertInto(cassandraKeyspace, "hashes")
        .value("hash", hash)

      MinerFactory.session.execute(statement)
    }

    def getPreHash: Option[String] = {
      // select query
      val selectStmt = select()
        .all()
        .from(cassandraKeyspace, "hashes")
        .limit(1)

      val resultSet = MinerFactory.session.execute(selectStmt)
      val row = resultSet.one()

      if (row != null) Option(row.getString("hash"))
      else None
    }

    def deletePreHash(): Unit = {
      MinerFactory.session.execute(s"TRUNCATE $cassandraKeyspace.hashes;")
    }

    def createBlock(block: Block): Unit = {
      // UDT
      val transType = MinerFactory.cluster.getMetadata.getKeyspace(cassandraKeyspace).getUserType("transaction")

      // transactions
      val transactions = block.transactions.map(t =>
        transType.newValue
          .setString("origin_zaddress", t.oriZaddr)
          .setUUID("id", t.id)
          .setString("from_zaddress", t.fromZaddr)
          .setString("to_zaddress", t.toZaddr)
          .setString("action", t.action)
          .setString("blob", t.blob)
          .setLong("timestamp", t.timestamp)
          .setString("digsig", t.digsig)
      ).asJava

      // insert query
      val statement = QueryBuilder.insertInto(cassandraKeyspace, "blocks")
        .value("miner", block.miner)
        .value("id", block.id)
        .value("transactions", transactions)
        .value("timestamp", block.timestamp)
        .value("merkle_root", block.merkleRoot)
        .value("pre_hash", block.preHash)
        .value("hash", block.hash)

      MinerFactory.session.execute(statement)
    }

    def getBlock(miner: String, id: UUID): Option[Block] = {
      // select query
      val selectStmt = select()
        .all()
        .from(cassandraKeyspace, "blocks")
        .where(QueryBuilder.eq("miner", miner)).and(QueryBuilder.eq("id", id))
        .limit(1)

      val resultSet = MinerFactory.session.execute(selectStmt)
      val row = resultSet.one()

      if (row != null) {
        // get transactions
        val t = row.getSet("transactions", classOf[UDTValue]).asScala.map(t =>
          Trans(t.getString("origin_zaddress"),
            t.getUUID("id"),
            t.getString("from_zaddress"),
            t.getString("to_zaddress"),
            t.getString("action"),
            t.getString("blob"),
            t.getLong("timestamp"),
            t.getString("digsig")
          )
        ).toList

        // get signatures
        val s = row.getSet("signatures", classOf[UDTValue]).asScala.map(s =>
          Signature(s.getString("miner"), s.getString("digsig"))
        ).toList

        // create block
        Option(
          Block(miner,
            id,
            t,
            row.getLong("timestamp"),
            row.getString("merkle_root"),
            row.getString("pre_hash"),
            row.getString("hash"),
            s)
        )
      }
      else None
    }

    def updateBlockSignature(block: Block, signature: Signature): Unit = {
      // signature type
      val sigType = MinerFactory.cluster.getMetadata.getKeyspace(cassandraKeyspace).getUserType("signature")

      // signature
      val sig = sigType.newValue.setString("miner", signature.miner).setString("digsig", signature.digsig)

      // existing signatures + new signature
      val sigs = block.signatures.map(s =>
        sigType.newValue
          .setString("miner", s.miner)
          .setString("digsig", s.digsig)
      ) :+ sig

      // update query
      val statement = QueryBuilder.update(cassandraKeyspace, "blocks")
        .`with`(QueryBuilder.add("signatures", sig))
        .where(QueryBuilder.eq("miner", block.miner)).and(QueryBuilder.eq("id", block.id))

      MinerFactory.session.execute(statement)
    }
  }

}

