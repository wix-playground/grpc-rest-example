package com.wix.grpc.rest

import com.google.api.HttpRule
import org.apache.commons.lang3.StringUtils.isNotEmpty
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod._

case class HttpMapping(method: HttpMethod, url: String) {
  def toUrlWithMethod = s"$method:$url"
}

object HttpMapping {
  def apply(method: String, url: String): HttpMapping = HttpMapping(valueOf(method.toUpperCase), url)

  def toHttpMappings(httpRule: HttpRule): Seq[HttpMapping] =
    Seq(
      withMethod(GET, httpRule.getGet),
      withMethod(POST, httpRule.getPost),
      withMethod(DELETE, httpRule.getDelete),
      withMethod(PUT, httpRule.getPut),
      withMethod(PATCH, httpRule.getPatch)
    ).flatten

  private def withMethod(method: HttpMethod, value: String) =
    if (isNotEmpty(value)) Some(HttpMapping(method, value)) else None
}
