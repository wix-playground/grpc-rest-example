package com.wix.grpc.rest

import javax.servlet.http.HttpServletRequest

import com.wix.core.services.identification.service.IdentificationService
import com.wix.grpc.RequestMetadata

trait MetadataProvider {
  def metadataFor(request: HttpServletRequest): RequestMetadata
}

class IdentificationServiceMetadataProvider(identificationService: IdentificationService) extends MetadataProvider {
  override def metadataFor(request: HttpServletRequest): RequestMetadata = {
    val cookies = Option(request.getCookies).toSeq
    val cookiesMap = cookies.flatten.map(c => (c.getName, c.getValue)).toMap
    RequestMetadata(identificationService.getIdentity(cookiesMap))
  }
}
