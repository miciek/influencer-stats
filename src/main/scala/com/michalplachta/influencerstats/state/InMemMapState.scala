package com.michalplachta.influencerstats.state
import cats.Monad
import cats.effect.Async
import cats.mtl.FunctorTell
import cats.implicits._
import com.michalplachta.influencerstats.core.model.Collection

import scala.collection.concurrent.TrieMap

class InMemMapState[F[_]: Monad: Async](implicit F: FunctorTell[F, String]) extends CollectionsState[F] {
  private val state = TrieMap.empty[String, Collection]

  def fetchCollection(id: String): F[Option[Collection]] = {
    for {
      _      <- F.tell(s"looking for collection with id $id")
      result <- Async[F].delay(state.get(id))
    } yield result
  }

  def saveCollection(id: String, collection: Collection): F[Unit] = {
    for {
      _ <- F.tell(s"saving collection $collection under id $id")
      _ <- Async[F].delay(state.put(id, collection))
    } yield ()
  }
}
