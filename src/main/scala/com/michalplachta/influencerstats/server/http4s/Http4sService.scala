package com.michalplachta.influencerstats.server.http4s

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import com.michalplachta.influencerstats.core.model.{Collection, InfluencerResults}
import com.michalplachta.influencerstats.state.CollectionsState
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

class Http4sService[F[_]: Monad: Sync: CollectionsState](getResults: String => F[InfluencerResults])
    extends Http4sDsl[F] {
  implicit def jsonDecoder[A <: Product: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def jsonEncoder[A <: Product: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  val routes = HttpRoutes.of[F] {
    case GET -> Root / "collections" / collectionId =>
      CollectionsState[F].fetchCollection(collectionId).flatMap {
        case Some(collection) => Ok(collection)
        case None             => NotFound()
      }

    case req @ PUT -> Root / "collections" / collectionId =>
      req
        .as[Collection]
        .flatMap { collection =>
          CollectionsState[F].saveCollection(collectionId, collection)
        }
        .flatMap(Ok(_))

    case GET -> Root / "collections" / collectionId / "stats" =>
      getResults(collectionId)
        .flatMap(Ok(_))
  }
}
