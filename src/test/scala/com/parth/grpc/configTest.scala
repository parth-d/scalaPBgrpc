package com.parth.grpc

import grpcClient.{config => configfile}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class configTest extends AnyFlatSpec with Matchers{
  behavior of "common configuration parameters"
  it should "obtain the time range" in {configfile.getInt("parameters.range") should be > 0}

  it should "obtain time" in {configfile.getString("parameters.time") should fullyMatch regex ".*:.*:.*\\..*"}

  it should "obtain key" in {configfile.getString("parameters.key") should fullyMatch regex ".*\\..*"}

  it should "obtain timeout" in {configfile.getInt("parameters.timeout") should be < 5}

  it should "obtain localhost port" in {configfile.getInt("parameters.port") should be (50051)}

//  behavior of "mr1 parameters"
//  it should "obtain temp path" in {configfile.getString("mr1.temp_path").length should be > 0}
//  it should "obtain output path" in {configfile.getString("mr1.output_path").length should be > 0}
}