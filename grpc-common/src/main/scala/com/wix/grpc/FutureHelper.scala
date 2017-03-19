package com.wix.grpc

import java.util.concurrent.Executor

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object FutureHelper {

  val synchronousExecutionContext = ExecutionContext.fromExecutor(new Executor {
    def execute(task: Runnable) = task.run()
  }, th => throw th)

  implicit def toFuture[T](value: T): Future[T] = Future.successful(value)
}
