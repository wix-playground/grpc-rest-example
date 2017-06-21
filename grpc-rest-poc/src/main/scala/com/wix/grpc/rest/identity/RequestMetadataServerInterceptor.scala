package com.wix.grpc.rest.identity

import com.wix.grpc.RequestMetadata
import io.grpc.ServerCall.Listener
import io.grpc._

class RequestMetadataServerInterceptor(storage: InMemoryRequestMetadataStorage) extends ServerInterceptor {

  override def interceptCall[ReqT, RespT](serverCall: ServerCall[ReqT, RespT], metadata: Metadata,
                                          serverCallHandler: ServerCallHandler[ReqT, RespT]): Listener[ReqT] = {
    val requestMetadataKey = metadata.get(MetadataKey)
    val ctx = Context.current().withValue(RequestMetadata.ContextKey, storage.get(requestMetadataKey))
    Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler)
  }
}
