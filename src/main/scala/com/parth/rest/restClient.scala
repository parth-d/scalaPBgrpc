package com.parth.rest

import akka.actor.ActorSystem
import akka.actor.TypedActor.{context, self}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}

import java.util.logging.Logger
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Await, Future}

class restClient

object restClient {

  /** Akka objects, used to create the ActorSystem object and its derivatives for the communication. */
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  import system.dispatcher

  /** Read values from application.conf */
  val config: Config            = ConfigFactory.load()
  val range: Int                = config.getInt("parameters.range")
  val time: String              = config.getString("parameters.time")
  val bucket: String            = config.getString("parameters.bucket")
  val key: String               = config.getString("parameters.key")
  val timeout: FiniteDuration   = config.getInt("parameters.timeout").seconds

  private[this] val logger = Logger.getLogger(classOf[restClient].getName)

  /**
   * The main execution function which creates a future object and stores the incoming values in those objects as and when they arrive.
   * @return A String object which will be 'completed' in the future.
   */
  def sendRequest(): Future[String] = {

    logger.info("Sending Request")
    val responseFuture: Future[HttpResponse] = Http().singleRequest(Get(s"https://a94he0mou3.execute-api.us-east-2.amazonaws.com/default/bucketextract?range=$range&time=$time&bucket=$bucket&key=$key"))

    val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(response => response.entity.toStrict(timeout))
    logger.info("Sent Request")

    entityFuture.map(entity => entity.data.utf8String)
  }

  /**
   * Driver code
   * @param args CMD arguments
   */
  def main(args: Array[String]): Unit ={

    logger.info("Started execution")
    System.out.println("\n\n" + Await.result(sendRequest(), timeout) + "\n\n")
    logger.info("Completed successfully")
  }
}
