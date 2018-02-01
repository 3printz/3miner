package com.score.cchain.comp

import java.util.UUID

import com.datastax.driver.core.UDTValue
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.score.cchain.protocol._
import com.score.cchain.util.DbFactory

import scala.collection.JavaConverters._

trait ChainDbCompImpl extends ChainDbComp {

  val chainDb = new ChainDbImpl

  class ChainDbImpl extends ChainDb {

    def getTrans: List[Trans] = {
      // select query
      val selectStmt = select()
        .all()
        .from("trans")

      // get all trans
      val resultSet = DbFactory.session.execute(selectStmt)
      resultSet.all().asScala.map { row =>
        Trans(row.getString("bank"),
          row.getUUID("id"),
          Cheque(
            row.getString("cheque_bank"),
            row.getUUID("cheque_id"),
            row.getInt("cheque_amount"),
            row.getString("cheque_date"),
            row.getString("cheque_img")),
          row.getString("from_acc"),
          row.getString("to_acc"),
          row.getLong("timestamp"),
          row.getString("digsig"),
          row.getString("state")
        )
      }.toList
    }

    def deleteTrans(trans: List[Trans]): Unit = {
      for (t <- trans) {
        // delete query
        val delStmt = delete()
          .from("trans")
          .where(QueryBuilder.eq("bank", t.bankId)).and(QueryBuilder.eq("id", t.id))

        DbFactory.session.execute(delStmt)
      }
    }

    def createPreHash(hash: String): Unit = {
      // insert query
      val statement = QueryBuilder.insertInto("hashes")
        .value("hash", hash)

      DbFactory.session.execute(statement)
    }

    def getPreHash: Option[String] = {
      // select query
      val selectStmt = select()
        .all()
        .from("hashes")
        .limit(1)

      val resultSet = DbFactory.session.execute(selectStmt)
      val row = resultSet.one()

      if (row != null) Option(row.getString("hash"))
      else None
    }

    def deletePreHash(): Unit = {
      DbFactory.session.execute(s"TRUNCATE cchain.hashes;")
    }

    def createBlock(block: Block): Unit = {
      // UDT
      val transType = DbFactory.cluster.getMetadata.getKeyspace("cchain").getUserType("transaction")

      // transactions
      val transactions = block.transactions.map(t =>
        transType.newValue
          .setString("bank", t.bankId)
          .setUUID("id", t.id)
          .setString("cheque_bank", t.cheque.bankId)
          .setUUID("cheque_id", t.cheque.id)
          .setInt("cheque_amount", t.cheque.amount)
          .setString("cheque_date", t.cheque.date)
          .setString("cheque_img", t.cheque.img)
          .setString("from_acc", t.from)
          .setString("to_acc", t.to)
          .setLong("timestamp", t.timestamp)
          .setString("digsig", t.digsig)
          .setString("state", t.state)
      ).asJava

      // insert query
      val statement = QueryBuilder.insertInto("blocks")
        .value("miner", block.miner)
        .value("id", block.id)
        .value("transactions", transactions)
        .value("timestamp", block.timestamp)
        .value("merkle_root", block.merkleRoot)
        .value("pre_hash", block.preHash)
        .value("hash", block.hash)

      DbFactory.session.execute(statement)
    }

    def getBlock(miner: String, id: UUID): Option[Block] = {
      // select query
      val selectStmt = select()
        .all()
        .from("blocks")
        .where(QueryBuilder.eq("miner", miner)).and(QueryBuilder.eq("id", id))
        .limit(1)

      val resultSet = DbFactory.session.execute(selectStmt)
      val row = resultSet.one()

      if (row != null) {
        // get transactions
        val t = row.getSet("transactions", classOf[UDTValue]).asScala.map(t =>
          Trans(t.getString("bank"),
            t.getUUID("id"),
            Cheque(
              t.getString("cheque_bank"),
              t.getUUID("cheque_id"),
              t.getInt("cheque_amount"),
              t.getString("cheque_date"),
              t.getString("cheque_img")),
            t.getString("from_acc"),
            t.getString("to_acc"),
            t.getLong("timestamp"),
            t.getString("digsig"),
            t.getString("state")
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
      val sigType = DbFactory.cluster.getMetadata.getKeyspace("cchain").getUserType("signature")

      // signature
      val sig = sigType.newValue.setString("miner", signature.miner).setString("digsig", signature.digsig)

      // existing signatures + new signature
      val sigs = block.signatures.map(s =>
        sigType.newValue
          .setString("miner", s.miner)
          .setString("digsig", s.digsig)
      ) :+ sig

      // update query
      val statement = QueryBuilder.update("blocks")
        .`with`(QueryBuilder.add("signatures", sig))
        .where(QueryBuilder.eq("miner", block.miner)).and(QueryBuilder.eq("id", block.id))

      DbFactory.session.execute(statement)
    }
  }

}

