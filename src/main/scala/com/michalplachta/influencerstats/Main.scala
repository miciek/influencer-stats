package com.michalplachta.influencerstats

import cats.effect.{IO, IOApp}
import cats.mtl.FunctorTell
import com.michalplachta.influencerstats.api._
import com.michalplachta.influencerstats.clients.Http4sClient
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.core.model.Collection
import com.michalplachta.influencerstats.logging.IoLogger
import com.michalplachta.influencerstats.state.InMemMapState
import com.typesafe.config.ConfigFactory

object Main extends IOApp {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")

  implicit val logging: FunctorTell[IO, String] = new IoLogger

  val state = new InMemMapState[IO]

  (1 to 10000).map(id => (id.toString, Collection(List.empty))).foreach {
    case (id, collection) =>
      state.saveCollection(id, collection).unsafeRunSync()
  }

  override def run(args: List[String]) = {
    Http4sServer
      .http4sServer(
        host,
        port,
        client =>
          Statistics.getInfluencerResults(
            state.fetchCollection,
            Http4sClient.getVideoListResponse(client, youtubeUri, youtubeApiKey)
        ),
        state.fetchCollection,
        state.saveCollection
      )
  }
}
