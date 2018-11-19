package com.michalplachta.influencerstats.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import cats.effect.IO
import com.michalplachta.influencerstats.core.model.InfluencerResults
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

object AkkaHttpRoutes extends Directives {
  def getInfluencerResults(getResults: String => IO[InfluencerResults]): Route = {
    path("collections" / Segment / "stats") { collectionId =>
      get {
        complete((StatusCodes.OK, getResults(collectionId).unsafeToFuture))
      }
    }
  }

  def putCollection(saveCollection: (String, Collection) => Unit): Route = {
    path("collections" / Segment) { collectionId =>
      put {
        entity(as[Collection]) { collection =>
          saveCollection(collectionId, collection)
          complete(StatusCodes.Created)
        }
      }
    }
  }

  def getCollection(getCollection: String => Option[Collection]): Route = {
    path("collections" / Segment) { collectionId =>
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