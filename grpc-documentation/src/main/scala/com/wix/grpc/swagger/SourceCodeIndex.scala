package com.wix.grpc.swagger

import com.google.protobuf.DescriptorProtos.FileDescriptorProto

import scala.collection.JavaConverters._

class SourceCodeIndex(file: FileDescriptorProto) {
  private val map: Map[List[Int], String] =
    file.getSourceCodeInfo.getLocationList.asScala
    .map(l => (att(l.getPathList.asScala), l.getLeadingComments))
    .toMap

  private def att(x: Seq[Integer]):List[Int] = x.map(_.toInt).toList

  def message(messageIndexes: Seq[Int]) = map(messagePath(messageIndexes))

  def service(serviceIndex: Int) = map(servicePath(serviceIndex))

  def serviceRpc(serviceIndex: Int, rpcIndex: Int) = map(serviceRpcPath(serviceIndex, rpcIndex))

  def field(methodIndexes: Seq[Int], fieldIndex: Int) = map(fieldPath(methodIndexes, fieldIndex))

  private def messagePath(methodIndexes: Seq[Int]) =
    4 :: methodIndexes.head :: methodIndexes.tail.flatMap(Seq(3, _)).toList

  private def fieldPath(methodIndexes: Seq[Int], fieldIndex: Int) =
    messagePath(methodIndexes) ::: (2 :: fieldIndex :: Nil)

  private def servicePath(serviceIndex: Int) = 6 :: serviceIndex :: Nil

  private def serviceRpcPath(serviceIndex: Int, rpcIndex: Int) =
    6 :: serviceIndex :: 2 :: rpcIndex :: Nil
}
