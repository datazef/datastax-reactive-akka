package com.datastax.demo.cql

import java.util.concurrent.CompletionStage

import com.datastax.demo.dao.MemberDAO
import com.datastax.oss.driver.api.core.CqlSession
import com.typesafe.scalalogging.Logger

import scala.concurrent.Future
import scala.concurrent.java8.FuturesConvertersImpl.{CF, P}

case class Cql() {
  private[cql] val logger = Logger[Cql]
  lazy private[cql] val session = createSession()

  logger.info("Launching cql dao ...")
  lazy val members = MemberDAO(session)

  private[cql] def createSession(): CqlSession = {
    CqlSession.builder.build
  }

  def init(): Unit = {
    logger.info("Initializing cql dao ...")
//    members.init()
  }
}

object Cql {
  implicit class CompletionStageExtension[T](cs: CompletionStage[T]) {
    def toScala: Future[T] = {
      cs match {
        case cf: CF[T] => cf.wrapped
        case _ =>
          val p = new P[T](cs)
          cs whenComplete p
          p.future
      }
    }
  }
}
