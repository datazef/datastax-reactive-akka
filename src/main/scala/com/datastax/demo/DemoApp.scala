package com.datastax.demo

import com.datastax.demo.akka.AkkaSystem
import com.datastax.demo.cql.Cql
import com.datastax.demo.http.HttpServer

object DemoApp extends App {
  val akka = AkkaSystem()
  val dao = Cql()
  val http = HttpServer("localhost",9000)(akka.system, dao)
  dao.init()
  http.bind()
}
