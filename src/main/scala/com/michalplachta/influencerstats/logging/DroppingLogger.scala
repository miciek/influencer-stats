package com.michalplachta.influencerstats.logging

import cats.effect.IO
import monix.execution.Scheduler
import monix.execution.atomic.Atomic
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

class DroppingLogger extends Logging[IO] {
  private val logger      = LoggerFactory.getLogger("dropping-logger")
  private val pendingLogs = Atomic(new ArrayBuffer[String](1000))

  Scheduler.fixedPool(name = "dropping-logger", poolSize = 1).scheduleWithFixedDelay(0.seconds, 1.second) {
    val logs = pendingLogs.getAndSet(new ArrayBuffer[String](1000))
    logs.foreach { msg =>
      logger.info(msg)
    }
  }

  def info(msg: String): IO[Unit] = {
    IO {
      pendingLogs.transform { logs =>
        if (logs.size < 1000) logs :+ msg
        else if (logs.size == 1000) logs :+ "Some logs were dropped, because the rate is higher than 1k/sec"
        else logs
      }
    }
  }
}
