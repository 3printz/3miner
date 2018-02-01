package com.score.cchain.comp

import com.score.cchain.protocol.{Block, Trans}


trait ChainDbComp {

  val chainDb: ChainDb

  trait ChainDb {
    def getTrans: List[Trans]

    def deleteTrans(trans: List[Trans])

    def createPreHash(hash: String): Unit

    def getPreHash: Option[String]

    def deletePreHash(): Unit

    def createBlock(block: Block): Unit
  }

}

