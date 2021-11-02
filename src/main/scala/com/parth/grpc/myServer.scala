package com.parth.grpc

import io.grpc.examples.helloworld.log.{GreeterGrpc, LambdaReply, LambdaRequest}
import io.grpc.{Server, ServerBuilder}

import java.util.logging.Logger
import scala.concurrent.{ExecutionContext, Future}

object myServer{
  private val logger = Logger.getLogger(classOf[myServer].getName)

  def main(args: Array[String]): Unit = {
    val server = new myServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
  private val port = 50051
}

class myServer(executionContext: ExecutionContext) {self =>
  private[this] var server: Server = null

  private def start(): Unit = {
    server = ServerBuilder.forPort(myServer.port).addService(GreeterGrpc.bindService(new GreeterImpl, executionContext)).build.start
    myServer.logger.info("Server started, listening on " + myServer.port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class GreeterImpl extends GreeterGrpc.Greeter {
    override def findLog(req: LambdaRequest) = {
      val exec = awsCaller(req.range, req.time, req.bucket, req.key)
      val reply = LambdaReply(exec)
      Future.successful(reply)
    }
  }
}