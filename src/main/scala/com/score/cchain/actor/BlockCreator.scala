package com.score.cchain.actor

import akka.actor.{Actor, Props}
import com.score.cchain.comp.ChainDbCompImpl
import com.score.cchain.config.AppConf
import com.score.cchain.protocol.{Block, Signature}
import com.score.cchain.util.{BlockFactory, RSAFactory, SenzLogger}

import scala.concurrent.duration._

object BlockCreator {

  case class Create()

  def props = Props(classOf[BlockCreator])

}

class BlockCreator extends Actor with ChainDbCompImpl with AppConf with SenzLogger {

  import BlockCreator._
  import context._

  val senzActor = context.actorSelection("/user/SenzActor")

  override def preStart(): Unit = {
    logger.debug("Start actor: " + context.self.path)
  }

  override def receive: Receive = {
    case Create =>
      // take trans, preHash from db and create block
      val trans = chainDb.getTrans
      val preHash = chainDb.getPreHash.getOrElse("")
      if (trans.nonEmpty) {
        val timestamp = System.currentTimeMillis
        val merkleRoot = BlockFactory.merkleRoot(trans)
        val hash = BlockFactory.hash(timestamp.toString, merkleRoot, preHash)
        val block = Block(bankId = senzieName,
          hash = hash,
          transactions = trans,
          timestamp = timestamp,
          merkleRoot = merkleRoot,
          preHash = preHash
        )
        chainDb.createBlock(block)

        // set block hash as the preHash
        chainDb.deletePreHash()
        chainDb.createPreHash(hash)

        // then sign
        val sig = RSAFactory.sign(block.hash)
        chainDb.updateBlockSignature(block, Signature(senzieName, sig))

        logger.debug("block created")

        // delete all transaction saved in the block from trans table
        chainDb.deleteTrans(block.transactions)

        // broadcast senz about the new block
        //senzActor ! Msg(SenzFactory.blockSignSenz(block.id.toString))
      } else {
        logger.debug("No trans to create block" + context.self.path)
      }

      // reschedule to create
      context.system.scheduler.scheduleOnce(miningDuration.seconds, self, Create)
  }
}
