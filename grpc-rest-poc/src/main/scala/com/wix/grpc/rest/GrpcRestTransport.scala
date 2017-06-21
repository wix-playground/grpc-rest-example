package com.wix.grpc.rest

import com.google.protobuf.DynamicMessage
import com.wix.grpc.rest.DefaultGrpcRestTransport._
import com.wix.grpc.rest.identity._
import com.wix.grpc.{GrpcServicesRegistry, RequestMetadata}
import io.grpc.inprocess.{InProcessChannelBuilder, InProcessServerBuilder}
import io.grpc.stub.ClientCalls
import io.grpc.{CallOptions, ServerInterceptors}

trait GrpcRestTransport {
  def start(): Unit

  def stop(): Unit

  def call(endpoint: GrpcRestEndpoint, input: DynamicMessage, metadata: RequestMetadata): DynamicMessage
}

class DefaultGrpcRestTransport(metadataStorage: InMemoryRequestMetadataStorage,
                               registry: GrpcServicesRegistry) extends GrpcRestTransport {
  private val server = {
    val requestMetadataServerInterceptor = new RequestMetadataServerInterceptor(metadataStorage)
    val builder = InProcessServerBuilder.forName(InProcessServerName).directExecutor()
    registry.services.map(_.definition) foreach { service =>
      builder.addService(ServerInterceptors.intercept(service, requestMetadataServerInterceptor))
    }
    builder.build()
  }
  private lazy val channel = InProcessChannelBuilder.forName(InProcessServerName)
    .directExecutor()
    .intercept(RequestMetadataClientInterceptor)
    .build()

  override def start(): Unit = server.start()

  override def stop(): Unit = server.shutdown()

  override def call(endpoint: GrpcRestEndpoint, input: DynamicMessage, metadata: RequestMetadata): DynamicMessage = {
    val key = metadataStorage.add(metadata)
    try {
      val options = CallOptions.DEFAULT.withOption(CallOptionKey, key)
      ClientCalls.blockingUnaryCall(channel, endpoint.methodDescriptor, options, input)
    } finally {
      metadataStorage.remove(key)
    }
  }
}

object DefaultGrpcRestTransport {
  val InProcessServerName = "RestGrpcTransport"
}