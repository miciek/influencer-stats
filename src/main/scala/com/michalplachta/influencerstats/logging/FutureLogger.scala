package com.michalplachta.influencerstats.logging

import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

class FutureLogger(implicit ec: ExecutionContext) extends Logging[Future] {
  private val logger = LoggerFactory.getLogger("future-logger")

  def info(msg: String): Future[Unit] = {
    Future(logger.info(msg))
  }
}
