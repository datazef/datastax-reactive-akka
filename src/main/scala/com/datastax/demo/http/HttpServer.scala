package com.datastax.demo.http

import java.net.InetSocketAddress
import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import akka.stream.scaladsl.{Flow, Sink}
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.Logger
import akka.util.ByteString
import com.datastax.demo.akka.{AkkaRequestProcessor, AkkaSingleton, AkkaSystem}
import com.datastax.demo.cql.Cql
import com.datastax.demo.protocol.GetNext
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.Promise

final class ImperativeRequestContext(ctx: RequestContext, promise: Promise[RouteResult]) {
  private implicit val ec = ctx.executionContext
  def complete(obj: ToResponseMarshallable): Unit = ctx.complete(obj).onComplete(promise.complete)
  def fail(error: Throwable): Unit = ctx.fail(error).onComplete(promise.complete)
}

case class HttpServer(interface:String, port: Int)(implicit akkaSystem: AkkaSystem, dao: Cql, singleton: AkkaSingleton) {
  private[http] val logger = Logger[HttpServer]
  private[http] val newline = ByteString("\n")
  lazy implicit val system = akkaSystem.system

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

  def imperativelyComplete(inner: ImperativeRequestContext => Unit): Route = { ctx: RequestContext =>
    val p = Promise[RouteResult]()
    inner(new ImperativeRequestContext(ctx, p))
    p.future
  }

  def provideRoutes(address: InetSocketAddress): Route = {
    logRequest("HttpServer", Logging.InfoLevel) {
      pathPrefix("public") {
        path("members") {
          get {
            complete(dao.members.reactiveAll)
          }
        } ~ path("counters") {
          get {
            imperativelyComplete { ctx =>
              system.actorOf(Props(AkkaRequestProcessor(ctx, singleton)),UUID.randomUUID().toString) ! GetNext
            }
          }
        }
      }
    }
  }
}
