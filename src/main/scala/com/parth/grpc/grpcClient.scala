package com.parth.grpc

import com.typesafe.config.{Config, ConfigFactory}
import io.grpc.examples.helloworld.log.GreeterGrpc.GreeterBlockingStub
import io.grpc.examples.helloworld.log.{GreeterGrpc, LambdaRequest}
import io.grpc.{ManagedChannel, ManagedChannelBuilder, StatusRuntimeException}

import java.util.concurrent.TimeUnit
import java.util.logging.{Level, Logger}

object grpcClient {

  /** Read values from application.conf */
  val config: Config  = ConfigFactory.load()
  val range: Int      = config.getInt("parameters.range")
  val time: String    = config.getString("parameters.time")
  val bucket: String  = config.getString("parameters.bucket")
  val key: String     = config.getString("parameters.key")
  val port: Int       = config.getInt("parameters.port")

  private[this] val logger = Logger.getLogger(classOf[grpcClient].getName)

  /**
   * Driver function to make this class runnable.
   * @param host localhost in our case
   * @param port predefined port in application.conf
   * @return grpcClient object
   */
  def apply(host: String, port: Int): grpcClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build
    val blockingStub = GreeterGrpc.blockingStub(channel)
    new grpcClient(channel, blockingStub)
  }

  /**
   * Driver function
   * @param args CMD arguments
   */
  def main(args: Array[String]): Unit = {
    logger.info("Starting grpcClient:" + "" +
      "port\t: " + port)
    val client = grpcClient("localhost", port)
    try client.find(range, time, bucket, key)
    finally client.shutdown()
  }
}

class grpcClient private(private val channel: ManagedChannel, private val blockingStub: GreeterBlockingStub) {
  private[this] val logger = Logger.getLogger(classOf[grpcClient].getName)

  def shutdown(): Unit = {
      logger.info("Trying to shutdown")
      channel.shutdown.awaitTermination(ConfigFactory.load().getLong("parameters.timeout"), TimeUnit.SECONDS)
    }

  /**
   * Main function which processes the response from grpcServer and outputs log statements according to the result.
   * @param range Time difference from absolute time to be considered for searching log files
   * @param time Absolute time around which to look out for in the logs
   * @param bucket S3 bucket name
   * @param key S3 file name
   */
  def find(range: Int, time: String, bucket: String, key: String): Unit = {
      val request = LambdaRequest(range, time, bucket, key)
      logger.info("Request created")
      try {
        val response = blockingStub.findLog(request)
        logger.info("Response: " + response.result)
        if (response.result.toInt == -1)
          logger.info("No log statements found for given parameters")
        else
          logger.info("Log message(s) at (and around) Index " + response.result + " fits the given parameters.")
      }
      catch {
        case e: StatusRuntimeException =>
          logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus)
      }
    }
}