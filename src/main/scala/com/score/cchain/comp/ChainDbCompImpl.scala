package com.score.cchain.comp

import java.util.UUID

import com.datastax.driver.core.UDTValue
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.score.cchain.config.CassandraConf
import com.score.cchain.protocol._
import com.score.cchain.util.MinerzFactory

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
      val resultSet = MinerzFactory.session.execute(selectStmt)
      resultSet.all().asScala.map { row =>
        Trans(row.getString("bank"),
          row.getUUID("id"),
          Cheque(
            row.getString("promize_bank"),
            row.getUUID("promize_id"),
            row.getString("promize_amount"),
            row.getString("promize_blob"),
            null, null, null),
          row.getString("from_account"),
          row.getString("from_bank"),
          row.getString("from_zaddress"),
          row.getString("to_account"),
          row.getString("to_bank"),
          row.getString("to_zaddress"),
          row.getLong("timestamp"),
          row.getString("digsig"),
          row.getString("type")
        )
      }.toList
    }

    def deleteTrans(trans: List[Trans]): Unit = {
      for (t <- trans) {
        // delete query
        val delStmt = delete()
          .from(cassandraKeyspace, "trans")
          .where(QueryBuilder.eq("bank", t.bank)).and(QueryBuilder.eq("id", t.id))

        MinerzFactory.session.execute(delStmt)
      }
    }

    def createPreHash(hash: String): Unit = {
      // insert query
      val statement = QueryBuilder.insertInto(cassandraKeyspace, "hashes")
        .value("hash", hash)

      MinerzFactory.session.execute(statement)
    }

    def getPreHash: Option[String] = {
      // select query
      val selectStmt = select()
        .all()
        .from(cassandraKeyspace, "hashes")
        .limit(1)

      val resultSet = MinerzFactory.session.execute(selectStmt)
      val row = resultSet.one()

      if (row != null) Option(row.getString("hash"))
      else None
    }

    def deletePreHash(): Unit = {
      MinerzFactory.session.execute(s"TRUNCATE $cassandraKeyspace.hashes;")
    }

    def createBlock(block: Block): Unit = {
      // UDT
      val transType = MinerzFactory.cluster.getMetadata.getKeyspace(cassandraKeyspace).getUserType("transaction")

      // transactions
      val transactions = block.transactions.map(t =>
        transType.newValue
          .setString("bank", t.bank)
          .setUUID("id", t.id)
          .setString("promize_bank", t.cheque.bankId)
          .setUUID("promize_id", t.cheque.id)
          .setString("promize_amount", t.cheque.amount)
          .setString("promize_blob", t.cheque.blob)
          .setString("from_bank", t.fromBank)
          .setString("from_account", t.fromAccount)
          .setString("from_zaddress", t.fromZaddress)
          .setString("to_bank", t.toBank)
          .setString("to_account", t.toAccount)
          .setString("to_zaddress", t.toZaddress)
          .setLong("timestamp", t.timestamp)
          .setString("digsig", t.digsig)
          .setString("type", t._type)
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

      MinerzFactory.session.execute(statement)
    }

    def getBlock(miner: String, id: UUID): Option[Block] = {
      // select query
      val selectStmt = select()
        .all()
        .from(cassandraKeyspace, "blocks")
        .where(QueryBuilder.eq("miner", miner)).and(QueryBuilder.eq("id", id))
        .limit(1)

      val resultSet = MinerzFactory.session.execute(selectStmt)
      val row = resultSet.one()

      if (row != null) {
        // get transactions
        val t = row.getSet("transactions", classOf[UDTValue]).asScala.map(t =>
          Trans(t.getString("bank"),
            t.getUUID("id"),
            Cheque(
              t.getString("promize_bank"),
              t.getUUID("promize_id"),
              t.getString("promize_amount"),
              t.getString("promize_blob"),
              null, null, null),
            t.getString("from_account"),
            t.getString("from_bank"),
            t.getString("from_zaddress"),
            t.getString("to_account"),
            t.getString("to_bank"),
            t.getString("to_zaddress"),
            t.getLong("timestamp"),
            t.getString("digsig"),
            t.getString("type")
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
      val sigType = MinerzFactory.cluster.getMetadata.getKeyspace(cassandraKeyspace).getUserType("signature")

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

      MinerzFactory.session.execute(statement)
    }
  }

}

