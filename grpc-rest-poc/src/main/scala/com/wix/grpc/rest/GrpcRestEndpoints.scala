package com.wix.grpc.rest

import com.google.protobuf.DescriptorProtos.MethodDescriptorProto
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.DynamicMessage
import com.google.protobuf.descriptor.MethodOptions
import com.trueaccord.scalapb.grpc.AbstractService
import com.wix.grpc.rest.HttpMapping.toHttpMappings
import com.wix.grpc.{GrpcService, GrpcServicesRegistry}
import io.grpc.MethodDescriptor
import io.grpc.MethodDescriptor.MethodType
import io.grpc.protobuf.ProtoUtils

import scala.collection.JavaConversions._
import scala.language.existentials

case class GrpcRestEndpoints(endpoints: Seq[GrpcRestEndpoint])

case class GrpcRestEndpoint(mapping: HttpMapping, methodDescriptor: MethodDescriptor[DynamicMessage, DynamicMessage],
                            inputType: Descriptor, outputType: Descriptor)

object GrpcRestEndpoints {

  def apply(registry: GrpcServicesRegistry): GrpcRestEndpoints =
    GrpcRestEndpoints(registry.services flatMap serviceEndpoints)

  private def serviceEndpoints(service: GrpcService[_ <: AbstractService]): Seq[GrpcRestEndpoint] = {
    val proto = service.service.serviceCompanion.javaDescriptor.toProto
    val parser = proto.getParserForType

    for {
      method <- proto.getMethodList
      httpMapping <- httpMappings(method)
      grpcMethod <- grpcUnaryMethod(service, method.getName)
    } yield {
      val descriptor = service.service.serviceCompanion.javaDescriptor.findMethodByName(method.getName)
      GrpcRestEndpoint(
        httpMapping,
        withDynamicMarshallers(grpcMethod.getMethodDescriptor, descriptor.getInputType, descriptor.getOutputType),
        descriptor.getInputType,
        descriptor.getOutputType
      )
    }
  }

  private def grpcUnaryMethod(service: GrpcService[_ <: AbstractService], methodName: String) =
    Option(service.definition.getMethod(s"${service.fullName}/$methodName"))
      .filter(_.getMethodDescriptor.getType == MethodType.UNARY)

  private def httpMappings(method: MethodDescriptorProto) = {
    val bytes = method.getOptions.toByteArray
    val scalaOption = MethodOptions.parseFrom(bytes)
    com.google.api.AnnotationsProto.http.get(scalaOption) map toHttpMappings getOrElse Seq.empty
  }

  private def withDynamicMarshallers(descriptor: MethodDescriptor[_, _], input: Descriptor, output: Descriptor) =
    descriptor.toBuilder(dynamicMarhallerFor(input), dynamicMarhallerFor(output)).build()

  private def dynamicMarhallerFor(messageType: Descriptor) =
    ProtoUtils.marshaller(DynamicMessage.getDefaultInstance(messageType))
}
