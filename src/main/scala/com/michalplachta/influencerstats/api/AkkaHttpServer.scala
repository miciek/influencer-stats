package com.michalplachta.influencerstats.api

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import cats.effect._
import com.michalplachta.influencerstats.core.model.InfluencerResults

object AkkaHttpServer {
  def akkaHttpServer(host: String,
                     port: Int,
                     getResults: String => IO[InfluencerResults],
                     getCollection: String => Option[Collection],
                     saveCollection: (String, Collection) => Unit)(implicit system: ActorSystem): IO[Unit] = IO {
    val httpApp = new HttpApp {
      override protected def routes: Route =
        Route.seal(
          AkkaHttpRoutes.getInfluencerResults(getResults) ~
          AkkaHttpRoutes.getCollection(getCollection) ~
          AkkaHttpRoutes.putCollection(saveCollection)
        )
    }
    httpApp.startServer(host, port, system)
  }
}
