package com.wix.grpc

import com.wix.core.services.identification.service.Identity
import io.grpc.Context

case class RequestMetadata(identity: Identity)

object RequestMetadata {
  val ContextKey = Context.key[RequestMetadata]("x-request-metadata")

  def fromContext(context: Context): RequestMetadata = ContextKey.get(context)

  def fromContext: RequestMetadata = fromContext(Context.current())
}