package com.wix.grpc.swagger

import java.io.File

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import protocbridge.ProtocCodeGenerator

object SwaggerGenerator extends ProtocCodeGenerator {
  private val mapper = new ObjectMapper

  override def run(request: CodeGeneratorRequest): CodeGeneratorResponse = {
    val swagger = SourceCodeToMap.swagger(request.getProtoFileList)
    mapper.writeValue(new File("swagger.json"), swagger)

    // return stub for now, we don't care about response
    CodeGeneratorResponse.newBuilder().build()
  }
}
