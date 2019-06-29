package com.michalplachta.influencerstats.server.akkahttp

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import cats.effect.IO
import com.michalplachta.influencerstats.core.model.CollectionStats
import com.michalplachta.influencerstats.state.{CollectionUpdate, CollectionView}

class AkkaHttpServer(host: String, port: Int)(implicit system: ActorSystem,
                                              collectionView: CollectionView[IO],
                                              collectionUpdate: CollectionUpdate[IO]) {
  def serve(getResults: String => IO[CollectionStats]): IO[Unit] = IO {
    val httpApp = new HttpApp {
      override protected def routes: Route =
        Route.seal(
          AkkaHttpRoutes.getInfluencerResults(getResults) ~
          AkkaHttpRoutes.getCollection(collectionView.fetchCollection) ~
          AkkaHttpRoutes.putCollection(collectionUpdate.saveCollection)
        )
    }
    httpApp.startServer(host, port, system)
  }
}
