package com.score.cchain.actor

import akka.actor.{Actor, Props}
import com.score.cchain.actor.Produzer.Produze
import com.score.cchain.config.{AppConf, KafkaConf}
import com.score.cchain.db.ChainDbCompImpl
import com.score.cchain.protocol.{Block, Signature}
import com.score.cchain.util.{BlockFactory, RSAFactory, SenzLogger}

import scala.concurrent.duration._

object BlockCreator {

  case class Create()

  def props = Props(classOf[BlockCreator])

}

class BlockCreator extends Actor with ChainDbCompImpl with AppConf with KafkaConf with SenzLogger {

  import BlockCreator._
  import context._

  // kafka producer actor
  val produzer = context.actorSelection("/user/Produzer")

  // start to create blocks
  self ! Create

  override def receive: Receive = {
    case Create =>
      // take trans, preHash from db and create block
      val trans = chainDb.getTrans
      val preHash = chainDb.getPreHash.getOrElse("")
      if (trans.nonEmpty) {
        // create block
        val timestamp = System.currentTimeMillis
        val merkleRoot = BlockFactory.merkleRoot(trans)
        val hash = BlockFactory.hash(timestamp.toString, merkleRoot, preHash)
        val block = Block(miner = senzieName,
          hash = hash,
          transactions = trans,
          timestamp = timestamp,
          merkleRoot = merkleRoot,
          preHash = preHash
        )
        chainDb.createBlock(block)

        // set block hash as the preHash
        // then sign block
        // finally delete all transaction saved in the block from trans table
        chainDb.deletePreHash()
        chainDb.createPreHash(hash)
        chainDb.updateBlockSignature(block, Signature(senzieName, RSAFactory.sign(block.hash)))
        chainDb.deleteTrans(block.transactions)

        // publish message to kafka topic regarding new block
        val senz = s"DATA #id ${block.id.toString} #time ${block.timestamp} #tras ${trans.size} @blockz ^minerz digsig"
        produzer ! Produze(kafkaTopic, senz)

        logger.debug("block created and published to kafka")
      } else {
        logger.debug("No trans to create block" + context.self.path)
      }

      // reschedule to create
      context.system.scheduler.scheduleOnce(miningInterval.seconds, self, Create)
  }

}
