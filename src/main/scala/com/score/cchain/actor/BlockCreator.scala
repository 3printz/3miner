package com.score.cchain.actor

import akka.actor.{Actor, Props}
import com.score.cchain.config.AppConf
import com.score.cchain.db.ChainDbCompImpl
import com.score.cchain.protocol.Trans
import com.score.cchain.util.SenzLogger
import scala.concurrent.duration._

object BlockCreator {

  case class Create()

  case class Tick()

  def props = Props(classOf[BlockCreator])

}

class BlockCreator extends Actor with ChainDbCompImpl with AppConf with SenzLogger {

  import BlockCreator._
  import context._

  context.system.scheduler.schedule(5.seconds, miningInterval.seconds, self, Tick)

  // start to create blocks
  self ! Create

  var x = 0

  override def receive: Receive = {
    case Create =>
      // reschedule to create
      val trans = Trans(
        oriZaddr = "0775432015",
        fromZaddr = "0775432015",
        toZaddr = "0675432015",
        action = "create",
        blob = "sdsd",
        digsig = "sdsds"
      )
      chainDb.createTrans(trans)
      self ! Create
      x = x +1
      logger.debug(s"x.. $x")
    case Tick =>
      logger.debug(s"created trans $x")
      self ! Create

      context.system.scheduler.scheduleOnce(miningInterval.seconds, self, Create)
  }
}
