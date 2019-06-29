package com.michalplachta.influencerstats.logging

import cats.effect.IO
import monix.execution.Scheduler
import monix.execution.atomic.Atomic
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
