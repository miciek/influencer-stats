package com.michalplachta.influencerstats.cache
import cats.effect.IO
import cats.implicits._
import com.michalplachta.influencerstats.core.model.InfluencerResults
import com.michalplachta.influencerstats.state.AllCollectionsView
import monix.execution.Scheduler

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._

class StatisticsCaching(getInfluencerResults: String => IO[InfluencerResults])(implicit state: AllCollectionsView[IO]) {
  private val cache = TrieMap.empty[String, InfluencerResults]

  Scheduler.fixedPool(name = "statistics-caching", poolSize = 1).scheduleWithFixedDelay(0.seconds, 10.seconds) {
    refreshCache.unsafeRunSync()
  }

  def refreshCache: IO[Unit] = {
    for {
      ids <- state.fetchAllCollectionIds
      _ <- ids.map { id =>
            getInfluencerResults(id).flatMap { influencerResults =>
              IO {
                cache.put(id, influencerResults)
              }
            }
          }.sequence
    } yield ()
  }

  def getCachedInfluencerResults(id: String): IO[InfluencerResults] = {
    IO {
      cache.getOrElse(id, InfluencerResults.empty)
    }
  }
}
