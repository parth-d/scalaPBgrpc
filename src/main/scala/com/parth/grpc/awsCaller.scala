package com.parth.grpc
//import akka.actor.TypedActor.dispatcher
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.stream.ActorMaterializer

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

object awsCaller {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher


  def apply(range: Int, time: String, bucket: String, key: String): String = {
    val rtn = Await.result(sendRequest(range, time, bucket, key), 5000 millis)
    rtn
  }

  def sendRequest(range: Int, time: String, bucket: String, key: String): Future[String] = {
//    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
    val uri = "https://l8g4lzzz9h.execute-api.us-east-2.amazonaws.com/default/gRPC_Boolean?range=" + range + "&time=" + time + "&bucket=" + bucket + "&key=" + key
    val responseFuture: Future[HttpResponse] = Http().singleRequest(Get(uri))
    val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(response => response.entity.toStrict(2.seconds))
    entityFuture.map(entity => entity.data.utf8String)
  }
}
