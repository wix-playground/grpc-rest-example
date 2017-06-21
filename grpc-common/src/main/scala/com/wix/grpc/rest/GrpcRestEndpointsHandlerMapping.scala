package com.wix.grpc.rest

import javax.servlet.http._

import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping

class GrpcRestEndpointsHandlerMapping(endpoints: GrpcRestEndpoints) extends AbstractUrlHandlerMapping {
  endpoints.endpoints foreach { e =>
    registerHandler(
      toUrlWithMethod(e.mapping.method.toString, e.mapping.url),
      e
    )
  }

  override def lookupHandler(urlPath: String, request: HttpServletRequest): AnyRef = {
    super.lookupHandler(toUrlWithMethod(request.getMethod, urlPath), request)
  }

  private def toUrlWithMethod(method: String, url: String) = s"$method:$url"
}