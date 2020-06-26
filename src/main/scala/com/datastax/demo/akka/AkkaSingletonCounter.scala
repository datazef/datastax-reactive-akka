package com.datastax.demo.akka

import akka.actor.Actor
import com.datastax.demo.protocol.{Bootstrap, GetNext}
import com.typesafe.scalalogging.Logger

case class AkkaSingletonCounter() extends Actor {
  private[akka] val logger = Logger[AkkaSingletonCounter]

  logger.info(s"Starting counter actor")

  var counter:Int = _

  override def receive: Receive = {
    case Bootstrap =>
      counter = 0

    case GetNext =>
      logger.info(s"Request received. Counter current value is: $counter")
      counter += 1
      sender() ! counter
  }
}
