package com.michalplachta.influencerstats.logging
import cats.Functor
import cats.effect.IO
import cats.mtl.DefaultFunctorTell
import org.slf4j.LoggerFactory

class IoLogger extends DefaultFunctorTell[IO, String] {
  private val logger   = LoggerFactory.getLogger("io-logger")
  override val functor = Functor[IO]

  override def tell(msg: String): IO[Unit] = {
    IO {
      logger.info(msg)
    }
  }
}
