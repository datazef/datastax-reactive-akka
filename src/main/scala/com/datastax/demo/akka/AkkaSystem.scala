package com.datastax.demo.akka

import akka.actor.ActorSystem
import akka.cluster.Cluster
import com.typesafe.scalalogging.Logger

case class AkkaSystem() {
  private[akka] val logger = Logger[AkkaSystem]

  logger.info("Launching akka system...")
  val system = ActorSystem()
  val cluster = Cluster(system)
  cluster.join(cluster.selfMember.address)
}
