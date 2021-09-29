package com.marionete.lenses

import org.apache.curator.framework.recipes.shared.SharedCount
import org.apache.kafka.connect.source.SourceRecord

import java.util
import java.util.UUID

object MyConverterSpec extends App{

  private val sourcePartition = new util.HashMap[String, Any]()
  private val path = s"/foo/${UUID.randomUUID().toString}"
  sourcePartition.put("path", path)
  val sourceRecord = new SourceRecord(sourcePartition, new util.HashMap[String, Any](), "topic", null, "hello world")
  private val converter = new MyConverter()
  converter.convert(sourceRecord)

  val client = converter.getCuratorFramework
  val counter = new SharedCount(client, path, 0)
  counter.start()

  println(s"Counter in Zookeeper ${counter.getCount}")

}
