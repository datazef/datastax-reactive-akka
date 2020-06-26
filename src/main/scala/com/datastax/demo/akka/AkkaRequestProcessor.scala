package com.datastax.demo.akka

import akka.actor.{Actor, ReceiveTimeout}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import com.datastax.demo.http.ImperativeRequestContext
import com.datastax.demo.protocol.GetNext
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.duration._

case class AkkaRequestProcessor(ctx: ImperativeRequestContext, singleton: AkkaSingleton) extends Actor {
  import context._
  private[akka] val logger = Logger[AkkaRequestProcessor]

  setReceiveTimeout(5.seconds)

  override def receive: Receive = {
    case GetNext =>
      logger.info(s"Sending request to the proxy")
      singleton.proxy ! GetNext
      become(waitAndReply)
  }

  def waitAndReply: Receive = {
    case counter: Int =>
      complete(counter)
    case ReceiveTimeout =>
      logger.warn(s"Timeout")
      complete(StatusCodes.InternalServerError)
  }

  def complete(m: => ToResponseMarshallable): Unit = {
    ctx.complete(m)
    stop(self)
  }
}
