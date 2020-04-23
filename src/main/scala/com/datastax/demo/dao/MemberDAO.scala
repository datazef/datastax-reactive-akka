package com.datastax.demo.dao

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.querybuilder.QueryBuilder
import com.datastax.oss.driver.api.querybuilder.select.Selector
import com.typesafe.scalalogging.Logger
import com.datastax.demo.cql.Cql._
import com.datastax.demo.dto.Member
import com.datastax.oss.driver.api.core.cql.{PreparedStatement, Row}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

case class MemberDAO(session: CqlSession) {
  private[MemberDAO] val logger = Logger[MemberDAO]
  private[MemberDAO] val selectors = Seq(Selector.column("member_uuid"), Selector.column("first_name"),
    Selector.column("last_name"), Selector.column("email"),
    Selector.column("age"), Selector.column("created_date"))

  var get_all_statement: PreparedStatement = _

  def init(): Unit = {
    logger.info("Creating get_all_statement ...")

    session.prepareAsync(QueryBuilder.selectFrom("gatling", "members")
      .selectors(selectors: _*).build()).toScala onComplete {
      case Success(result) =>
        logger.info("Prepared statement get_all_statement is ready!")
        get_all_statement = result
      case Failure(exception) =>
        logger.error(s"Failed to create the statement get_all_statement due to ${exception.getMessage}")
    }
  }

  def fromRow(row : Row):Member = {
    Member(
      row.getUuid("member_uuid").toString,
      row.getString("first_name"),
      row.getString("last_name"),
      row.getString("email"),
      row.getInt("age"),
      row.getInstant("created_date").toEpochMilli
    )
  }

  def reactiveAll: Source[Member, NotUsed] = {
    Source
      .fromPublisher(session.executeReactive(get_all_statement.bind()))
      .map(fromRow)
  }
}
