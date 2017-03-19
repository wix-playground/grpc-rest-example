package com.wix.grpc.server

import com.wix.grpc.GrpcServicesRegistry
import io.grpc.ServerBuilder

import scala.language.existentials

trait GrpcServer {

  def start(): Unit

  def stop(): Unit
}

class DefaultGrpcServer(registry: GrpcServicesRegistry, port: Int) extends GrpcServer {
  private val server = {
    val builder = ServerBuilder.forPort(port).directExecutor()
    registry.services.map(_.definition) foreach builder.addService
    builder.build()
  }

  override def start(): Unit = server.start()

  override def stop(): Unit = server.shutdown()
}