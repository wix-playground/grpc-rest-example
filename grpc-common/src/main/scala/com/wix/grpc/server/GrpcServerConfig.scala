package com.wix.grpc.server

import com.wix.grpc.GrpcServicesRegistry
import com.wixpress.framework.security.management.ManagementPortProvider
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class GrpcServerConfig {

  @Bean(initMethod = "start", destroyMethod = "stop")
  def grpcServer(registry: GrpcServicesRegistry, portProvider: ManagementPortProvider): GrpcServer = {
    // HACK to find port
    val managementPort = (0 until 65536) find portProvider.isManagementPort
    new DefaultGrpcServer(registry, managementPort.get - 3)
  }
}
