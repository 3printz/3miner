package com.score.cchain.comp

import com.score.cchain.protocol.{Block, Transaction}


trait ChainDbComp {

  val chainDb: ChainDb

  trait ChainDb {

    def getTransactions: List[Transaction]

    def deleteTransactions(transactions: List[Transaction])

    def createBlock(block: Block): Unit
  }

}

