package com.wix.grpc.rest.identity

import io.grpc.ClientCall.Listener
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc._

object RequestMetadataClientInterceptor extends ClientInterceptor {
  override def interceptCall[ReqT, RespT](methodDescriptor: MethodDescriptor[ReqT, RespT],
                                          callOptions: CallOptions, channel: Channel): ClientCall[ReqT, RespT] = {
    new SimpleForwardingClientCall[ReqT, RespT](channel.newCall(methodDescriptor, callOptions)) {
      override def start(responseListener: Listener[RespT], headers: Metadata): Unit = {
        headers.put(MetadataKey, callOptions.getOption(CallOptionKey))
        super.start(responseListener, headers)
      }
    }
  }
}
