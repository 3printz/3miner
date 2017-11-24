package com.score.cchain.comp

import com.score.cchain.protocol.{Block, Transaction}


trait ChainDbComp {

  val chainDb: ChainDb

  trait ChainDb {

    def getTransactions: List[Transaction]

    def deleteTransactions(transactions: List[Transaction])

    def createPreHash(hash: String): Unit

    def getPreHash: Option[String]

    def deletePreHash(): Unit

    def createBlock(block: Block): Unit
  }

}

