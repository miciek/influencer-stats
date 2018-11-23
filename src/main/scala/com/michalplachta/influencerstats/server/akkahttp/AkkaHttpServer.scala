package com.michalplachta.influencerstats.server.akkahttp
import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import cats.effect.IO
import com.michalplachta.influencerstats.core.model.InfluencerResults
import com.michalplachta.influencerstats.server.Server
import com.michalplachta.influencerstats.state.CollectionsState

class AkkaHttpServer(implicit state: CollectionsState[IO], system: ActorSystem) extends Server[IO] {

  def serve(host: String, port: Int, getResults: String => IO[InfluencerResults]): IO[Unit] = IO {
    val httpApp = new HttpApp {
      override protected def routes: Route =
        Route.seal(
          AkkaHttpRoutes.getInfluencerResults(getResults) ~
          AkkaHttpRoutes.getCollection(state.fetchCollection) ~
          AkkaHttpRoutes.putCollection(state.saveCollection)
        )
    }
    httpApp.startServer(host, port, system)
  }
}
