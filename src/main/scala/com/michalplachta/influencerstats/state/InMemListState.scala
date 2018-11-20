package com.michalplachta.influencerstats.state

import cats.Monad
import cats.effect.Async
import cats.implicits._
import cats.mtl.FunctorTell
import com.michalplachta.influencerstats.api.Collection
import monix.execution.atomic.Atomic

class InMemListState[F[_]: Monad: Async](implicit F: FunctorTell[F, String]) {
  val state = Atomic(List.empty[(String, Collection)])

  def fetchCollection(id: String): F[Option[Collection]] = {
    def find(collections: List[(String, Collection)]): F[Option[Collection]] = {
      collections match {
        case x :: xs =>
          for {
            _          <- F.tell(s"checking if $x is the collection we are looking for")
            collection <- if (x._1 == id) Async[F].pure(Some(x._2)) else find(xs)
          } yield collection
        case Nil =>
          Async[F].pure(None)
      }
    }

    for {
      _      <- F.tell(s"looking for collection with id $id")
      result <- find(state.get)
    } yield result
  }

  def saveCollection(id: String, collection: Collection): F[Unit] = {
    for {
      _ <- F.tell(s"saving collection $collection under id $id")
      _ <- Async[F].delay(state.transform(_ :+ ((id, collection))))
      _ <- F.tell(s"state now contains ${state.get.size} collections")
    } yield ()
  }
}
