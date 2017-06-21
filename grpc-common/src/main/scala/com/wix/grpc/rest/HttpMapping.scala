package com.wix.grpc.rest

import com.google.api.HttpRule
import org.apache.commons.lang3.StringUtils.isNotEmpty
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod._

case class HttpMapping(method: HttpMethod, url: String, body: Option[String])

object HttpMapping {
  def toHttpMappings(httpRule: HttpRule): Seq[HttpMapping] = {
    val body = Option(httpRule.body).filter(b => !b.isEmpty && b != "*")
    Seq(
      withMethod(GET, httpRule.getGet, body),
      withMethod(POST, httpRule.getPost, body),
      withMethod(DELETE, httpRule.getDelete, body),
      withMethod(PUT, httpRule.getPut, body),
      withMethod(PATCH, httpRule.getPatch, body)
    ).flatten
  }

  private def withMethod(method: HttpMethod, value: String, body: Option[String]) =
    if (isNotEmpty(value)) Some(HttpMapping(method, value, body)) else None
}
