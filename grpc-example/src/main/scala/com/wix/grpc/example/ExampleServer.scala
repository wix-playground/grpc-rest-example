package com.wix.grpc.example

import com.wix.bootstrap.jetty.BootstrapServer
import com.wix.grpc.GrpcServicesRegistry
import com.wix.grpc.example.api.v1.accounts.AccountsGrpc
import com.wix.grpc.example.api.v1.countries.CountriesGrpc
import com.wix.grpc.rest.GrpcRestServicesConfig
import com.wix.grpc.server.GrpcServerConfig
import org.springframework.context.annotation.{Bean, Import}

object ExampleServer extends BootstrapServer {
  override def additionalSpringConfig: Option[Class[_]] = Some(classOf[ServerWiring])
}

@Import(Array(classOf[GrpcServerConfig], classOf[GrpcRestServicesConfig]))
class ServerWiring {

  @Bean def grpcServicesRegistry: GrpcServicesRegistry = GrpcServicesRegistry.Empty
      .withService(new CountriesImpl, CountriesGrpc.bindService)
      .withService(new AccountsImpl, AccountsGrpc.bindService)
}
