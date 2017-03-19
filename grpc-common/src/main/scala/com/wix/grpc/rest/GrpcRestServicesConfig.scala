package com.wix.grpc.rest

import com.wix.grpc.GrpcServicesRegistry
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class GrpcRestServicesConfig {

  @Bean
  def grpcRestEndpoints(registry: GrpcServicesRegistry): GrpcRestEndpoints = GrpcRestEndpoints(registry)

  @Bean
  def grpcRestEndpointsHandlerMapping(endpoints: GrpcRestEndpoints) = new GrpcRestEndpointsHandlerMapping(endpoints)

  @Bean(initMethod = "start", destroyMethod = "stop")
  def grpcRestTransport(registry: GrpcServicesRegistry) = new DefaultGrpcRestTransport(registry)

  @Bean
  def grpcRestHandlerAdapter(transport: GrpcRestTransport) = new GrpcRestHandlerAdapter(transport)
}
