package com.wix.grpc.rest

import com.wix.core.services.identification.service.IdentificationServiceImpl
import com.wix.grpc.GrpcServicesRegistry
import com.wix.grpc.rest.identity.InMemoryRequestMetadataStorage
import com.wixpress.framework.security.{SecurityRequestAspectIntegrationSpringConfig, SessionReader}
import org.springframework.context.annotation.{Bean, Configuration, Import}

@Configuration
@Import(Array(classOf[SecurityRequestAspectIntegrationSpringConfig]))
class GrpcRestServicesConfig {

  @Bean
  def grpcRestEndpoints(registry: GrpcServicesRegistry): GrpcRestEndpoints = GrpcRestEndpoints(registry)

  @Bean
  def grpcRestEndpointsHandlerMapping(endpoints: GrpcRestEndpoints) = new GrpcRestEndpointsHandlerMapping(endpoints)

  @Bean(initMethod = "start", destroyMethod = "stop")
  def grpcRestTransport(registry: GrpcServicesRegistry) =
    new DefaultGrpcRestTransport(new InMemoryRequestMetadataStorage, registry)

  @Bean
  def grpcRestHandlerAdapter(transport: GrpcRestTransport, metadataProvider: MetadataProvider) =
    new GrpcRestHandlerAdapter(transport, metadataProvider)

  @Bean
  def metadataProvider(sessionReader: SessionReader): MetadataProvider = {
    // TODO: here should be Boaz service - but it doesn't work
    new IdentificationServiceMetadataProvider(new IdentificationServiceImpl(sessionReader))
  }
}
