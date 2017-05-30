package com.wix.grpc.example

import com.wix.bootstrap.RunServer
import com.wix.bootstrap.RunServer.Options
import com.wix.e2e.http

object ITEnv {
  val serverPort = http.Implicits.defaultServerPort.port

  RunServer(ExampleServer, Options(port = Some(serverPort)))
}
