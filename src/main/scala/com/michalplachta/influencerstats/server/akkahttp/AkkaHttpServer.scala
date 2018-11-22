package com.michalplachta.influencerstats.server.akkahttp
import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import cats.effect.IO
import com.michalplachta.influencerstats.core.model.{Collection, InfluencerResults}
import com.michalplachta.influencerstats.server.Server

class AkkaHttpServer extends Server[IO] {
  private val system: ActorSystem = ActorSystem("akka-http-server")

  def serve(host: String,
            port: Int,
            getResults: String => IO[InfluencerResults],
            getCollection: String => IO[Option[Collection]],
            saveCollection: (String, Collection) => IO[Unit]): IO[Unit] = IO {
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
