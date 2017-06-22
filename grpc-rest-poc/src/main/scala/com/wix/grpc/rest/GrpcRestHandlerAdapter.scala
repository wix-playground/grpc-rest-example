package com.wix.grpc.rest

import java.io.Reader
import java.util.{Map => JMap}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.{DynamicMessage, Message}
import com.wixpress.hoopoe.json.JsonMapper.Implicits.global
import com.wixpress.hoopoe.json._
import org.apache.commons.io.IOUtils
import org.springframework.http.HttpMethod._
import org.springframework.web.servlet.{HandlerAdapter, HandlerMapping, ModelAndView}

import scala.collection.JavaConverters._
import scala.collection.mutable

class GrpcRestHandlerAdapter(transport: GrpcRestTransport, metadataProvider: MetadataProvider) extends HandlerAdapter {

  override def handle(request: HttpServletRequest, response: HttpServletResponse, handler: scala.Any): ModelAndView = {
    val endpoint = handler.asInstanceOf[GrpcRestEndpoint]
    val input = httpRequestToGrpcInput(request, endpoint.mapping, endpoint.inputType)
    val metadata = metadataProvider.metadataFor(request)

    val outputMessage = transport.call(endpoint, input, metadata)

    grpcOutputToHttpResponse(outputMessage, response)
    null
  }

  override def supports(handler: scala.Any): Boolean = handler.isInstanceOf[GrpcRestEndpoint]

  override def getLastModified(request: HttpServletRequest, handler: scala.Any): Long = -1L

  private def httpRequestToGrpcInput(request: HttpServletRequest, mapping: HttpMapping,
                                     inputType: Descriptor): DynamicMessage = {
    val builder = DynamicMessage.newBuilder(inputType)
    val requestMap = mutable.Map.empty[String, Any]
    if (requestWithBody(request)) {
      requestMap ++= readBody(request.getReader, mapping, builder)
    }

    val additional = pathParams(request) ++ request.getParameterMap.asScala.mapValues(_(0))
    for ((key, value) <- additional) {
      putDeep(key.split("\\.").toList, value, requestMap)
    }
    JsonFormat.parser().merge(requestMap.asJsonStr, builder)
    builder.build()
  }

  private def requestWithBody(request: HttpServletRequest) = {
    val requestMethod = valueOf(request.getMethod.toUpperCase)
    !(requestMethod == GET || requestMethod == DELETE)
  }

  private def pathParams(request: HttpServletRequest): Map[String, String] = {
    val attr = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)
    if (attr == null) Map.empty else attr.asInstanceOf[JMap[String, String]].asScala.toMap
  }

  private def grpcOutputToHttpResponse(output: Message, response: HttpServletResponse): Unit = {
    val writer = response.getWriter
    JsonFormat.printer().appendTo(output, writer)
  }

  private def readBody(reader: Reader, mapping: HttpMapping, builder: DynamicMessage.Builder): Map[String, Any] = {
    val jsonContent = IOUtils.toString(reader).as[Map[String, Any]]
    mapping.body.map(_ -> jsonContent).map(Map(_)).getOrElse(jsonContent)
  }

  private def putDeep(key: List[String], value: Any, current: mutable.Map[String, Any]): Unit = key match {
    case head :: Nil => current += head -> value
    case head :: tail =>
      val nested = current.getOrElseUpdate(head, new mutable.HashMap[String, Any]).asInstanceOf[mutable.Map[String, Any]]
      putDeep(tail, value, nested)
  }
}
