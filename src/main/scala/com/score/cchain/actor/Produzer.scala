package com.score.cchain.actor

import akka.actor.{Actor, Props}
import com.score.cchain.actor.Produzer.Produze
import com.score.cchain.kafka.KafkaProduzer
import com.score.cchain.util.SenzLogger
import org.apache.kafka.clients.producer.ProducerRecord

object Produzer {

  case class Produze(topic: String, senz: String)

  def props = Props(classOf[Produzer])

}

class Produzer extends Actor with KafkaProduzer with SenzLogger {

  override def receive: Receive = {
    case Produze(topic, senz) =>
      // produce message
      val record = new ProducerRecord[String, String](topic, senz)
      producer.send(record)

      logger.info(s"produced message $senz to $topic")
  }

}

