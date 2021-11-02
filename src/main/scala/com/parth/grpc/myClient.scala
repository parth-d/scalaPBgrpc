package com.parth.grpc

import io.grpc.{ManagedChannel, ManagedChannelBuilder, StatusRuntimeException}
import io.grpc.examples.helloworld.log.GreeterGrpc
import io.grpc.examples.helloworld.log.GreeterGrpc.GreeterBlockingStub
import io.grpc.examples.helloworld.log.{LambdaRequest, LambdaReply}

import java.util.concurrent.TimeUnit
import java.util.logging.{Level, Logger}

object myClient {
  def apply(host: String, port: Int): myClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build
    val blockingStub = GreeterGrpc.blockingStub(channel)
    new myClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {
    val client = myClient("localhost", 50051)
    try {
      val range = 1
      val time = "04:01:00.000"
      val bucket = "logsinput"
      val key = "log.log"
      client.find(range, time, bucket, key)
    } finally {
      client.shutdown()
    }
  }
}

class myClient private(
  private val channel: ManagedChannel,
  private val blockingStub: GreeterBlockingStub
  ) {
    private[this] val logger = Logger.getLogger(classOf[myClient].getName)

    def shutdown(): Unit = {
      channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
    }

    /** Say hello to server. */
    def find(range: Int, time: String, bucket: String, key: String): Unit = {
//      logger.info("Will try to greet " + name1 + " ...")
      val request = LambdaRequest(range, time, bucket, key)
      try {
        val response = blockingStub.findLog(request)
        logger.info("Greeting: " + response.result)
      }
      catch {
        case e: StatusRuntimeException =>
          logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus)
      }
    }
}