package com.score.cchain.actor

import java.util.UUID

import akka.actor.{Actor, Props}
import com.score.cchain.db.ChainDbCompImpl
import com.score.cchain.config.AppConf
import com.score.cchain.protocol.{Msg, Signature}
import com.score.cchain.util.{RSAFactory, SenzFactory, SenzLogger}

object BlockSigner {

  case class SignBlock(miner: Option[String], blockId: Option[String])

  def props = Props(classOf[BlockSigner])

}

class BlockSigner extends Actor with ChainDbCompImpl with AppConf with SenzLogger {

  import BlockSigner._

  val senzActor = context.actorSelection("/user/SenzActor")

  override def preStart(): Unit = {
    logger.debug("Start actor: " + context.self.path)
  }

  override def receive: Receive = {
    case SignBlock(Some(miner), Some(blockId)) =>
      // extract block from db
      chainDb.getBlock(miner, UUID.fromString(blockId)) match {
        case Some(b) =>
          // sign block hash
          val sig = RSAFactory.sign(b.hash)

          // update signature in db
          chainDb.updateBlockSignature(b, Signature(senzieName, sig))

          // response back signed = true
          senzActor ! Msg(SenzFactory.blockSignResponseSenz(blockId.toString, miner, signed = true))
        case None =>
          // response back signed = false
          senzActor ! Msg(SenzFactory.blockSignResponseSenz(blockId.toString, miner, signed = false))
      }

      context.stop(self)
  }

}
