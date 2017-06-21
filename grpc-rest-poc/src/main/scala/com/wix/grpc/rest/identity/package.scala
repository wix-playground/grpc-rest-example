package com.wix.grpc.rest

import io.grpc.Metadata.BinaryMarshaller
import io.grpc.{CallOptions, Metadata}

package object identity {
  val CallOptionKey = CallOptions.Key.of[Long]("x-request-metadata-key-bin", -1)
  val MetadataKey = Metadata.Key.of[Long]("x-request-metadata-key-bin", new BinaryMarshaller[Long] {
    override def toBytes(t: Long): Array[Byte] = BigInt(t).toByteArray
    override def parseBytes(bytes: Array[Byte]): Long = BigInt(bytes).toLong
  })
}
