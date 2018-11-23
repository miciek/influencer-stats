package com.michalplachta.influencerstats.logging
import cats.effect.Sync
import org.slf4j.LoggerFactory

class DefaultLogger[F[_]: Sync] extends Logging[F] {
  private val logger = LoggerFactory.getLogger("default-logger")

  def info(msg: String): F[Unit] = {
    Sync[F].delay {
      logger.info(msg)
    }
  }
}
