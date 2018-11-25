package com.michalplachta.influencerstats.logging
import cats.effect.Sync
import monix.execution.Scheduler
import monix.execution.atomic.Atomic
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

class IoLogger[F[_]: Sync] extends Logging[F] {
  private val logger      = LoggerFactory.getLogger("io-logger")
  private val pendingLogs = Atomic(new ArrayBuffer[String](1000))

  Scheduler.fixedPool(name = "io-logger", poolSize = 1).scheduleWithFixedDelay(0.seconds, 1.second) {
    val logs = pendingLogs.getAndSet(new ArrayBuffer[String](1000))
    logs.foreach { msg =>
      logger.debug(msg)
    }
  }

  def debug(msg: String): F[Unit] = {
    Sync[F].delay {
      pendingLogs.transform { logs =>
        if (logs.size < 1000) logs :+ msg
        else if (logs.size == 1000) logs :+ "Some logs were dropped, because the rate is higher than 1k/sec"
        else logs
      }
    }
  }
}
