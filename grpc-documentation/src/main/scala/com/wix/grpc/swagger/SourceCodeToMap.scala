package com.wix.grpc.swagger

import java.util.{List => JList}

import com.google.protobuf.DescriptorProtos.{DescriptorProto, FieldDescriptorProto, FileDescriptorProto, ServiceDescriptorProto}
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType._
import io.swagger.models._
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.properties._

import scala.collection.JavaConverters._

object SourceCodeToMap {
  type Comment = String
  type Index = Int

  def swagger(file: JList[FileDescriptorProto]): Swagger = {
    val swagger = new Swagger()
    val messages = file.asScala.flatMap(f => methods(f).toSeq).toMap

    messages foreach { case(name, model) => swagger.model(name, model) }
    val services = file.asScala.flatMap { f =>
      val codeIndex = new SourceCodeIndex(f)
      val pack = "." + f.getPackage
      f.getServiceList.asScala.zipWithIndex.flatMap(t => toSwagger(t._1, t._2, pack, messages, codeIndex))
    }
    services foreach { s => swagger.path(s.name, s.path)}
    swagger
  }

  def methods(file: FileDescriptorProto): Map[String, Model] = {
    val codeIndex = new SourceCodeIndex(file)
    val messages = file.getMessageTypeList.asScala
      .zipWithIndex
      .flatMap {case (m, index) => nestedMessages(m, Seq(index), s".${file.getPackage}.${m.getName}")}
    val map = messages.map(m => (m.fullName, m)).toMap

    messages.map(m => (m.fullName, toSwagger(m, codeIndex, map))).toMap
  }

  private def nestedMessages(message: DescriptorProto, position: Seq[Int], name: String): Seq[Message] = {
    val nested = message.getNestedTypeList.asScala.view
      .zipWithIndex
      .flatMap { case (msg, currentIndex) => nestedMessages(msg, position :+ currentIndex, name + "." + msg.getName) }
    nested :+ Message(name, message, position)
  }

  private def toSwagger(service: ServiceDescriptorProto, serviceIndex: Int,
                        pack: String, messages: Map[String, Model],
                        codeIndex: SourceCodeIndex): Seq[RestOperation] = {
    val serviceName = s"$pack.${service.getName}"
    service.getMethodList.asScala.zipWithIndex map { case (m, index) =>
      val input = messages(m.getInputType)
      val output = messages(m.getOutputType)

      val operationId = s"$serviceName.${m.getName}"
      val operation = new Operation()
        .description(codeIndex.serviceRpc(serviceIndex, index))
        .operationId(operationId)
        .parameter(new BodyParameter().schema(input))
      RestOperation(operationId, new Path().get(operation))
    }
  }

  private def toSwagger(message: Message,
                        sourceCodeIndex: SourceCodeIndex,
                        messages: Map[String, Message]): Model = {
    val params = message.message.getFieldList.asScala
        .zipWithIndex
        .map {case(f, index) => toSwaggerField(f, sourceCodeIndex.field(message.position, index))}
    val model = new ModelImpl().description(sourceCodeIndex.message(message.position)).name(message.message.getName)
    model.setProperties(params.toMap.asJava)
    model
  }

  private def toSwaggerField(field: FieldDescriptorProto, comment: String): (String, Property) = {
    val property = FieldDescriptor.Type.valueOf(field.getType).getJavaType match {
      case STRING => new StringProperty()
      case BOOLEAN => new BooleanProperty()
      case INT => new IntegerProperty()
      case LONG => new LongProperty()
      case _ => new StringProperty()
    }
    property.title(field.getName)
            .description(comment)
    (field.getName, property)
  }
}

case class Message(fullName: String, message: DescriptorProto, position: Seq[Int])
case class RestOperation(name: String, path: Path)