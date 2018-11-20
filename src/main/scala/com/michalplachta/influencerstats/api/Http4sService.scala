package com.michalplachta.influencerstats.api

import cats.effect._
import com.michalplachta.influencerstats.core.model.{Collection, InfluencerResults}
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

class Http4sService(getResults: String => IO[InfluencerResults],
                    getCollection: String => IO[Option[Collection]],
                    saveCollection: (String, Collection) => IO[Unit]) {
  implicit def jsonDecoder[F[_]: Sync, A <: Product: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def jsonEncoder[F[_]: Sync, A <: Product: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "collections" / collectionId =>
      getCollection(collectionId).flatMap {
        case Some(collection) => Ok(collection)
        case None             => NotFound()
      }

    case req @ PUT -> Root / "collections" / collectionId =>
      req
        .as[Collection]
        .flatMap { collection =>
          saveCollection(collectionId, collection)
        }
        .flatMap(Ok(_))

    case GET -> Root / "collections" / collectionId / "stats" =>
      getResults(collectionId)
        .flatMap(Ok(_))
  }
}
