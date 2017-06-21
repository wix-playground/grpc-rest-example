package com.wix.grpc.rest

import com.google.protobuf.DynamicMessage
import com.wix.grpc.GrpcServicesRegistry
import io.grpc.CallOptions
import io.grpc.inprocess.{InProcessChannelBuilder, InProcessServerBuilder}
import io.grpc.stub.ClientCalls
import DefaultGrpcRestTransport._

trait GrpcRestTransport {
  def start(): Unit

  def stop(): Unit

  def call(endpoint: GrpcRestEndpoint, input: DynamicMessage): DynamicMessage
}

class DefaultGrpcRestTransport(registry: GrpcServicesRegistry) extends GrpcRestTransport {
  private val server = {
    val builder = InProcessServerBuilder.forName(InProcessServerName).directExecutor()
    registry.services.map(_.definition) foreach builder.addService
    builder.build()
  }
  private lazy val channel = InProcessChannelBuilder.forName(InProcessServerName)
    .directExecutor()
    .intercept()
    .build()

  override def start(): Unit = server.start()

  override def stop(): Unit = server.shutdown()

  override def call(endpoint: GrpcRestEndpoint, input: DynamicMessage): DynamicMessage =
    ClientCalls.blockingUnaryCall(channel, endpoint.methodDescriptor, CallOptions.DEFAULT, input)
}

object DefaultGrpcRestTransport {
  val InProcessServerName = "RestGrpcTransport"
}