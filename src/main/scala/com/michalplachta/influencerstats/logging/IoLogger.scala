package com.michalplachta.influencerstats.logging
import cats.Functor
import cats.effect.IO
import cats.mtl.DefaultFunctorTell
import monix.execution.Scheduler
import monix.execution.atomic.Atomic
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

class IoLogger extends DefaultFunctorTell[IO, String] {
  private val logger      = LoggerFactory.getLogger("io-logger")
  private val pendingLogs = Atomic(List.empty[String])

  Scheduler.singleThread(name = "io-logger-thread").scheduleWithFixedDelay(0.seconds, 1.second) {
    val logs = pendingLogs.getAndSet(List.empty)
    logs.foreach { msg =>
      logger.info(msg)
    }
  }

  override val functor = Functor[IO]
  override def tell(msg: String): IO[Unit] = {
    IO {
      pendingLogs.transform { logs =>
        if (logs.size < 1000) logs :+ msg
        else if (logs.size == 1000) logs :+ "Some logs were dropped, because the rate is higher than 1k/sec"
        else logs
      }
    }
  }
}
