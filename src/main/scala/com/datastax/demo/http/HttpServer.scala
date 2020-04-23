package com.datastax.demo.http

import java.net.InetSocketAddress
import java.util.UUID

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink}
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.Logger
import akka.util.ByteString
import com.datastax.demo.cql.Cql
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

case class HttpServer(interface:String, port: Int)(implicit system:ActorSystem, dao: Cql) {
  private[http] val logger = Logger[HttpServer]
  private[http] val newline = ByteString("\n")

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport
      .json()
      // comment out the lines below to comma-delimited JSON streaming
      .withFramingRenderer(
      // this enables new-line delimited JSON streaming
      Flow[ByteString].map(byteString => byteString ++ newline)
    )

  logger.info("Launching web interface...")

  def bind(): Unit = {
    logger.info("Initializing web interface...")

    Http().bind(interface, port).runWith(Sink foreach { conn =>
      conn.handleWith(route(conn.remoteAddress))
    })
  }

  def route(address: InetSocketAddress): Route = {
    cookiesRoutes(address)
  }

  def createXAuthCookie() = HttpCookie(
    name    = "DEMO_SESSION",
    value   = UUID.randomUUID().toString,
    expires = None,
    maxAge  = None,
  )

  def cookiesRoutes(address: InetSocketAddress): Route = {
    optionalCookie("DEMO_SESSION") {
      case Some(_) =>
        provideRoutes(address)
      case None =>
        setCookie(createXAuthCookie()) {
          provideRoutes(address)
        }
    }
  }

  def provideRoutes(address: InetSocketAddress): Route = {
    logRequest("HttpServer", Logging.InfoLevel) {
      pathPrefix("public") {
        get {
          complete(dao.members.reactiveAll)
        }
      }
    }
  }
}
