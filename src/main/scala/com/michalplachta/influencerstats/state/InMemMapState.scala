package com.michalplachta.influencerstats.state
import cats.Monad
import cats.effect.Async
import cats.mtl.FunctorTell
import cats.implicits._
import com.michalplachta.influencerstats.api.Collection

import scala.collection.concurrent.TrieMap

object InMemMapState {
  val state = TrieMap.empty[String, Collection]

  def fetchCollection[F[_]: Monad: Async](id: String)(implicit F: FunctorTell[F, String]): F[Option[Collection]] = {
    for {
      _      <- F.tell(s"looking for collection with id $id")
      result <- Async[F].delay(state.get(id))
    } yield result
  }
}
