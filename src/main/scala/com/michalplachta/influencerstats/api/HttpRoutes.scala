package com.michalplachta.influencerstats.api
import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import cats.effect.IO
import com.michalplachta.influencerstats.core.model.InfluencerResults
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

object HttpRoutes extends Directives {
  def getInfluencerResults(getResults: UUID => IO[InfluencerResults]): Route = {
    path("collections" / JavaUUID / "stats") { collectionId =>
      get {
        complete((StatusCodes.OK, getResults(collectionId).unsafeToFuture))
      }
    }
  }

  def putCollection(saveCollection: (UUID, Collection) => Unit): Route = {
    path("collections" / JavaUUID) { collectionId =>
      put {
        entity(as[Collection]) { collection =>
          saveCollection(collectionId, collection)
          complete(StatusCodes.Created)
        }
      }
    }
  }

  def getCollection(getCollection: UUID => Option[Collection]): Route = {
    path("collections" / JavaUUID) { collectionId =>
      get {
        getCollection(collectionId) match {
          case Some(collection) =>
            complete((StatusCodes.OK, collection))
          case None =>
            complete(StatusCodes.NotFound)
        }
      }
    }
  }
}
