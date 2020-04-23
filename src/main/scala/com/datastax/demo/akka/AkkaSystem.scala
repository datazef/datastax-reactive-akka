package com.datastax.demo.akka

import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger

case class AkkaSystem() {
  private[akka] val logger = Logger[AkkaSystem]

  logger.info("Launching akka system...")

  val system = ActorSystem("demo")
}
