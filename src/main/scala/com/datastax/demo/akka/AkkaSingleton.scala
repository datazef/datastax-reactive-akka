package com.datastax.demo.akka

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import com.typesafe.scalalogging.Logger

case class AkkaSingleton(system:ActorSystem) {
  private[akka] val logger = Logger[AkkaSystem]

  logger.info("Launching akka singleton manager...")

  lazy val manager = system.actorOf(
    ClusterSingletonManager.props(
      singletonProps = Props(classOf[AkkaSingletonCounter]),
      terminationMessage = PoisonPill,
      settings = ClusterSingletonManagerSettings(system)),
    name = "counter")

  logger.info("Launching akka singleton proxy...")

  lazy val proxy = system.actorOf(
    ClusterSingletonProxy.props(
      singletonManagerPath = manager.path.toStringWithoutAddress,
      settings = ClusterSingletonProxySettings(system)),
    name = "proxy")
}
