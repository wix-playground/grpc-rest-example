package com.wix.grpc

import com.trueaccord.scalapb.grpc.AbstractService
import io.grpc.ServerServiceDefinition
import FutureHelper._

import scala.concurrent.ExecutionContext

case class GrpcServicesRegistry(services: List[GrpcService[_ <: AbstractService]]) {
  def withService[T <: AbstractService, I <: T](service: I, binder: (T, ExecutionContext) => ServerServiceDefinition,
                                                executionContext: ExecutionContext = synchronousExecutionContext) =
    copy(services = GrpcService(service, binder, executionContext) :: services)
}

object GrpcServicesRegistry {
  val Empty = GrpcServicesRegistry(services = Nil)
}

case class GrpcService[T <: AbstractService](service: T,
                                     binder: (T, ExecutionContext) => ServerServiceDefinition,
                                     executionContext: ExecutionContext) {
  lazy val definition: ServerServiceDefinition = binder(service, executionContext)
  lazy val fullName: String = service.serviceCompanion.javaDescriptor.getFullName
}