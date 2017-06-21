package com.wix.grpc.rest.identity

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import com.wix.grpc.RequestMetadata

import scala.collection.JavaConverters._

class InMemoryRequestMetadataStorage {
  private val storage = new ConcurrentHashMap[Long, RequestMetadata].asScala
  private val counter = new AtomicLong()

  def add(requestMetadata: RequestMetadata): Long = {
    val key = counter.incrementAndGet()
    storage += key -> requestMetadata
    key
  }

  def get(key: Long): RequestMetadata = storage(key)

  def remove(key: Long): Unit = storage -= key
}
