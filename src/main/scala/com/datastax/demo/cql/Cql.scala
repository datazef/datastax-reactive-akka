package com.datastax.demo.cql

import com.datastax.demo.dao.MemberDAO
import com.datastax.oss.driver.api.core.CqlSession
import com.typesafe.scalalogging.Logger

case class CqlDao() {
  private[CqlDao] val logger = Logger[CqlDao]
  logger.info("Launching cql dao ...")

  lazy private[dao] val members = MemberDAO(session)
  lazy private[dao] val session = createSession()

  private[dao] def createSession(): CqlSession = {
    CqlSession.builder.build
  }

  def init(): Unit = {
    logger.info("Initializing cql dao ...")
    members.init()
  }
}
