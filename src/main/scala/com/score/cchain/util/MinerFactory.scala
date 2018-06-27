package com.score.cchain.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.score.cchain.config.{ElasticConf, SchemaConf}
import com.score.cchain.db.CassandraCluster

import scala.concurrent.Await
import scala.concurrent.duration._

object MinerFactory extends CassandraCluster with SchemaConf with ElasticConf with SenzLogger {

  def initSchema() {
    // create keyspace
    session.execute(schemaCreateKeyspace)

    // create UDT
    session.execute(schemaCreateTypeTransaction)
    session.execute(schemaCreateTypeSignature)

    // create tables
    session.execute(schemaCreateTableTrans)
    session.execute(schemaCreateTableTransactions)
    session.execute(schemaCreateTableBlocks)
    session.execute(schemaCreateTableHashes)
    session.execute(schemaCreateTableSenzies)

    // create index
    session.execute(schemaCreateFromIndex)
    session.execute(schemaCreateToIndex)
  }

  def initIndex(): Boolean = {
    implicit val system = ActorSystem()
    implicit val ec = system.dispatcher
    implicit val materializer = ActorMaterializer()
    implicit val timeout = 40.seconds

    def index(index: String): Boolean = {
      // index
      val u = s"http://$elasticHost:9200/$index"
      val j =
        s"""
        {
          "settings":{
            "keyspace": "zchain"
          },
          "mappings": {
            "$index" : {
              "discover" : ".*"
            }
          }
        }
      """

      logger.info(s"init index request uri $u")
      logger.info(s"init index request data $j")

      val req = HttpRequest(PUT, uri = u, entity = ByteString(j.stripLineEnd))
      val resp = Await.result(Http().singleRequest(req), timeout)

      logger.info(s"init index response: $resp")

      resp.status == StatusCodes.OK || resp.status == StatusCodes.BadRequest
    }

    // create indexes
    index("transactions") | index("blocks")
  }

}
