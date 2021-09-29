package com.marionete.lenses

import com.datamountaineer.streamreactor.connect.ftp.source.SourceRecordConverter
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex
import org.apache.curator.framework.recipes.shared.SharedCount
import org.apache.curator.retry.RetryNTimes
import org.apache.kafka.connect.source.SourceRecord

import java.util

class MyConverter extends SourceRecordConverter {

  override def convert(in: SourceRecord): util.List[SourceRecord] = {
    lockAndCounter(in)
  }

  override def configure(configs: util.Map[String, _]): Unit = {
    println("############################## Nothing to do here")
  }


  /**
   * For this test we run async each task to emulate a possible Kafka broker.
   * Once we create the client to communicate wth zooKeeper [CuratorFramework]
   *
   * First we create distributed counter [SharedCount] using the client and a specific path, which works just like a fileASystem (/counter/bla)
   * * Once we have the counter we can use [setCount(x)] to add a value into the counter or [getCount] to obtain the value.
   *
   * Then we crete a distributed lock [InterProcessSemaphoreMutex] using the client and a specific path.
   * * Once we have the sharedLock we can use [acquire] which it will block the next lines of code to be used by
   * * just one machine, and then we will release the lock with [release]
   */
  private def lockAndCounter(in: SourceRecord): util.List[SourceRecord] = {
    val path = in.sourcePartition.get("path").asInstanceOf[String]
    val client = getCuratorFramework
    val counter = new SharedCount(client, path, 0)
    counter.start()
    val sharedLock = new InterProcessSemaphoreMutex(client, path)
    sharedLock.acquire()
    val sourceRecords = if (counter.getCount == 0) {
      println("######## Sending event just once ##########")
      counter.setCount(1)
      val records = new util.ArrayList[SourceRecord]()
      records.add(in)
      records
    } else {
      println("######## Event already sent ##########")
      new util.ArrayList[SourceRecord]()
    }
    sharedLock.release()
    sourceRecords
  }


  /**
   * Here we create the [CuratorFramework] which behave like the client to communicate with Zookeeper to tell him what to do/
   * We use Retry policy to at least try 3 times with a backoff of 100ms between each time.
   */
  def getCuratorFramework = {
    val sleepMsBetweenRetries = 100
    val maxRetries = 3
    val retryPolicy = new RetryNTimes(maxRetries, sleepMsBetweenRetries)
    val client = CuratorFrameworkFactory.newClient("172.19.0.2:2181", retryPolicy)
    client.start()
    client
  }

}
