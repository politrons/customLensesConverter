package com.marionete.lenses

import com.datamountaineer.streamreactor.connect.ftp.source.SourceRecordConverter
import org.apache.kafka.connect.source.SourceRecord

import java.util

class MyConverter extends SourceRecordConverter {

  override def convert(in: SourceRecord): util.List[SourceRecord] = {
    println("########################################### SourceRecord received:" + in)
    val records = new util.ArrayList[SourceRecord]()
    records.add(in)
    records
  }

  override def configure(configs: util.Map[String, _]): Unit = {
    println("############################## Nothing to do here")
  }
}
