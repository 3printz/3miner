package com.score.cchain.comp

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.score.cchain.protocol._
import com.score.cchain.util.DbFactory

import scala.collection.JavaConverters._

trait ChainDbCompImpl extends ChainDbComp {

  val chainDb = new ChainDbImpl

  class ChainDbImpl extends ChainDb {

    def getTransactions: List[Transaction] = {
      // select query
      val selectStmt = select()
        .all()
        .from("transactions")

      // get all transactions
      val resultSet = DbFactory.session.execute(selectStmt)
      resultSet.all().asScala.map { row =>
        Transaction(row.getString("bank_id"),
          row.getUUID("id"),
          Cheque(
            row.getString("cheque_bank_id"),
            row.getUUID("cheque_id"),
            row.getInt("cheque_amount"),
            row.getString("cheque_date"),
            row.getString("cheque_img")),
          row.getString("from_acc"),
          row.getString("to_acc"),
          row.getLong("timestamp"),
          row.getString("digsig"),
          row.getString("status")
        )
      }.toList
    }

    def deleteTransactions(transactions: List[Transaction]): Unit = {
      for (t <- transactions) {
        // delete query
        val delStmt = delete()
          .from("transactions")
          .where(QueryBuilder.eq("bank_id", t.bankId)).and(QueryBuilder.eq("id", t.id))

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
      val trans = block.transactions.map(t =>
        transType.newValue
          .setString("bank_id", t.bankId)
          .setUUID("id", t.id)
          .setString("cheque_bank_id", t.cheque.bankId)
          .setUUID("cheque_id", t.cheque.id)
          .setInt("cheque_amount", t.cheque.amount)
          .setString("cheque_date", t.cheque.date)
          .setString("cheque_img", t.cheque.img)
          .setString("from_acc", t.from)
          .setString("to_acc", t.to)
          .setLong("timestamp", t.timestamp)
          .setString("digsig", t.digsig)
          .setString("status", t.status)
      ).asJava

      // insert query
      val statement = QueryBuilder.insertInto("blocks")
        .value("bank_id", block.bankId)
        .value("id", block.id)
        .value("transactions", trans)
        .value("timestamp", block.timestamp)
        .value("merkle_root", block.merkleRoot)
        .value("pre_hash", block.preHash)
        .value("hash", block.hash)

      DbFactory.session.execute(statement)
    }
  }

}

