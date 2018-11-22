package com.michalplachta.influencerstats

import cats.effect.IO
import cats.mtl.FunctorTell
import com.michalplachta.influencerstats.clients.HammockClient
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.core.model.Collection
import com.michalplachta.influencerstats.logging.IoLogger
import com.michalplachta.influencerstats.server.Server
import com.michalplachta.influencerstats.server.http4s.Http4sServer
import com.michalplachta.influencerstats.state.InMemMapState
import com.typesafe.config.ConfigFactory

object Main extends App {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")

  implicit val server: Server[IO]               = new Http4sServer
  implicit val logging: FunctorTell[IO, String] = new IoLogger

  val state = new InMemMapState[IO]

  (1 to 10000).map(id => (id.toString, Collection(List.empty))).foreach {
    case (id, collection) =>
      state.saveCollection(id, collection).unsafeRunSync()
  }

  Server[IO]
    .serve(
      host,
      port,
      Statistics.getInfluencerResults(
        state.fetchCollection,
        HammockClient.getVideoListResponse(youtubeUri, youtubeApiKey)
      ),
      state.fetchCollection,
      state.saveCollection
    )
    .unsafeRunSync()
}
