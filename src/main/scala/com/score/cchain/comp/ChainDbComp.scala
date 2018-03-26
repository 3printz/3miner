package com.score.cchain.comp

import java.util.UUID

import com.score.cchain.protocol.{Block, Trans}


trait ChainDbComp {

  val chainDb: ChainDb

  trait ChainDb {
    def getTrans: List[Trans]

    def deleteTrans(trans: List[Trans])

    def createPreHash(hash: String): Unit

    def getPreHash: Option[String]

    def deletePreHash(): Unit

    def getBlock(miner: String, id: UUID): Option[Block]

    def createBlock(block: Block): Unit
  }

}

