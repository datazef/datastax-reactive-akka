package com.datastax.demo

import com.datastax.demo.akka.{AkkaSingleton, AkkaSystem}
import com.datastax.demo.cql.Cql
import com.datastax.demo.http.HttpServer

object DemoApp extends App {
  implicit val akka = AkkaSystem()
  implicit val dao = Cql()
  implicit val singleton = AkkaSingleton(akka.system)
  val http = HttpServer("localhost",9000)
  dao.init()
  http.bind()
}
