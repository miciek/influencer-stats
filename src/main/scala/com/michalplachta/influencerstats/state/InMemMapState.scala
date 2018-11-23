package com.michalplachta.influencerstats.state
import cats.Monad
import cats.effect.Sync
import cats.implicits._
import com.michalplachta.influencerstats.core.model.Collection
import com.michalplachta.influencerstats.logging.Logging

import scala.collection.concurrent.TrieMap

class InMemMapState[F[_]: Monad: Sync: Logging] extends CollectionsState[F] {
  private val state = TrieMap.empty[String, Collection]

  def fetchCollection(id: String): F[Option[Collection]] = {
    for {
      _      <- Logging[F].info(s"looking for collection with id $id")
      result <- Sync[F].delay(state.get(id))
    } yield result
  }

  def saveCollection(id: String, collection: Collection): F[Unit] = {
    for {
      _ <- Logging[F].info(s"saving collection $collection under id $id")
      _ <- Sync[F].delay(state.put(id, collection))
    } yield ()
  }

  def fetchAllCollectionIds: F[List[String]] = {
    Sync[F].delay {
      state.keys.toList
    }
  }
}
