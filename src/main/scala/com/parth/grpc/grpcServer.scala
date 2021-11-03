package com.parth.grpc

import com.typesafe.config.ConfigFactory
import io.grpc.examples.helloworld.log.{GreeterGrpc, LambdaReply, LambdaRequest}
import io.grpc.{Server, ServerBuilder}

import java.util.logging.Logger
import scala.concurrent.{ExecutionContext, Future}

object grpcServer{

  private val logger = Logger.getLogger(classOf[grpcServer].getName)
  private val port = ConfigFactory.load().getInt("parameters.port")

  def main(args: Array[String]): Unit = {

    logger.info("Starting grpcServer.")
    val server = new grpcServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()

  }
}

class grpcServer(executionContext: ExecutionContext) {self =>

  private[this] var server: Server = null
  private val logger = Logger.getLogger(classOf[grpcServer].getName)

  /**
   * Starter function for the server
   */
  private def start(): Unit = {
    server = ServerBuilder.forPort(grpcServer.port).addService(GreeterGrpc.bindService(new GreeterImpl, executionContext)).build.start
    logger.info("Server started, listening on " + grpcServer.port)
    sys.addShutdownHook {
      System.err.println("shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("server shut down")
    }
  }

  private def stop(): Unit =
    if (server != null) server.shutdown()

  private def blockUntilShutdown(): Unit =
    if (server != null) server.awaitTermination()

  /**
   * ScalaPB implementation
   * This function calls awsCaller, which calls the lambda function on AWS.
   * Upon receiving response, it completes the future object.
   */
  private class GreeterImpl extends GreeterGrpc.Greeter {
    override def findLog(req: LambdaRequest): Future[LambdaReply] = {
      val exec = awsCaller(req.range, req.time, req.bucket, req.key)
      val reply = LambdaReply(exec)
      Future.successful(reply)
    }
  }
}
