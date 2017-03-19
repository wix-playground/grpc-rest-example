package com.wix.grpc.rest

import java.util.{Map => JMap}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.{DynamicMessage, Message}
import org.springframework.http.HttpMethod._
import org.springframework.web.servlet.{HandlerAdapter, HandlerMapping, ModelAndView}

import scala.collection.JavaConversions._

class GrpcRestHandlerAdapter(transport: GrpcRestTransport) extends HandlerAdapter {

  override def handle(request: HttpServletRequest, response: HttpServletResponse, handler: scala.Any): ModelAndView = {
    val endpoint = handler.asInstanceOf[GrpcRestEndpoint]
    val outputMessage = transport.call(endpoint, httpRequestToGrpcInput(request, endpoint.inputType))
    grpcOutputToHttpResponse(outputMessage, response)
    null
  }

  override def supports(handler: scala.Any): Boolean = handler.isInstanceOf[GrpcRestEndpoint]

  override def getLastModified(request: HttpServletRequest, handler: scala.Any): Long = -1L

  private def httpRequestToGrpcInput(request: HttpServletRequest, inputType: Descriptor): DynamicMessage = {
    val builder = DynamicMessage.newBuilder(inputType)
    if (requestWithBody(request)) {
      JsonFormat.parser().merge(request.getReader, builder)
    }
    val additional = pathParams(request) ++ request.getParameterMap.mapValues(_(0))
    for ((key, value) <- additional;
         fieldDescriptor <- Option(inputType.findFieldByName(key))) {
      builder.setField(fieldDescriptor, value)
    }
    builder.build()
  }

  private def requestWithBody(request: HttpServletRequest) = {
    val requestMethod = valueOf(request.getMethod.toUpperCase)
    !(requestMethod == GET || requestMethod == DELETE)
  }

  private def pathParams(request: HttpServletRequest): Map[String, String] = {
    val attr = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)
    if (attr == null) Map.empty else attr.asInstanceOf[JMap[String, String]].toMap
  }

  private def grpcOutputToHttpResponse(output: Message, response: HttpServletResponse): Unit = {
    val writer = response.getWriter
    JsonFormat.printer().appendTo(output, writer)
  }
}
