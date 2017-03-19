package com.wix.grpc.rest

import javax.servlet.http._

import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping

class GrpcRestEndpointsHandlerMapping(endpoints: GrpcRestEndpoints) extends AbstractUrlHandlerMapping {
  endpoints.endpoints foreach { e =>
    registerHandler(e.mapping.toUrlWithMethod, e)
  }

  override def lookupHandler(urlPath: String, request: HttpServletRequest): AnyRef =
    super.lookupHandler(HttpMapping(request.getMethod, urlPath).toUrlWithMethod, request)
}