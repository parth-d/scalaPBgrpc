package com.parth.grpc

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory

import java.util.logging.Logger
import scala.concurrent.duration.{DurationLong, FiniteDuration}
import scala.concurrent.{Await, Future}

class awsCaller

object awsCaller {

  /** Akka objects, used to create the ActorSystem object and its derivatives for the communication. */
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  import system.dispatcher

  private[this] val logger = Logger.getLogger(classOf[awsCaller].getName)

  /** Read values from application.conf */
  val timeout: FiniteDuration   = ConfigFactory.load().getInt("parameters.timeout").seconds

  /**
   * Driver function to make this class runnable.
   * Waits for the predefined time to get the complete future string return value from the sendRequest method.
   * @param range Time difference from absolute time to be considered for searching log files
   * @param time Absolute time around which to look out for in the logs
   * @param bucket S3 bucket name
   * @param key S3 file name
   * @return Processed string result from the Lambda function via the local gprcServer.
   */
  def apply(range: Int, time: String, bucket: String, key: String): String = {
    logger.info("Started awsCaller execution\n" +
      "range\t: " + range + "" +
      "time\t:" + time)
    logger.info("Calling sendRequest")
    Await.result(sendRequest(range, time, bucket, key), timeout)
  }

  /**
   * A function to generate the future String object and to store the incoming value in that object as and when it arrives.
   * @param range Time difference from absolute time to be considered for searching log files
   * @param time Absolute time around which to look out for in the logs
   * @param bucket S3 bucket name
   * @param key S3 file name
   * @return Future[String] object to hold the response from awsServer.
   */
  def sendRequest(range: Int, time: String, bucket: String, key: String): Future[String] = {
    val uri = "https://l8g4lzzz9h.execute-api.us-east-2.amazonaws.com/default/gRPC_Boolean?range=" + range + "&time=" + time + "&bucket=" + bucket + "&key=" + key
    val responseFuture: Future[HttpResponse] = Http().singleRequest(Get(uri))
    val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(response => response.entity.toStrict(timeout))
    logger.info("Returning from sendRequest")
    entityFuture.map(entity => entity.data.utf8String)
  }
}
