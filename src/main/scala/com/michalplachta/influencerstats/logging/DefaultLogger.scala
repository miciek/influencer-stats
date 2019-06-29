package com.michalplachta.influencerstats.logging

import cats.effect.IO
import org.slf4j.LoggerFactory

class DefaultLogger extends Logging[IO] {
  private val logger = LoggerFactory.getLogger("default-logger")

  def info(msg: String): IO[Unit] = {
    IO {
      logger.info(msg)
    }
  }
}
