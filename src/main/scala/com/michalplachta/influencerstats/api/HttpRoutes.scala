package com.michalplachta.influencerstats.api
import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.michalplachta.influencerstats.core.model.InfluencerResults
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.Future

object HttpRoutes extends Directives {
  def getInfluencerResults(getResults: UUID => Future[InfluencerResults]): Route = {
    path("stats" / JavaUUID) { collectionId =>
      get {
        complete((StatusCodes.OK, getResults(collectionId)))
      }
    }
  }
}
