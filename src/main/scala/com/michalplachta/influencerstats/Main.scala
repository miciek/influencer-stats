package com.michalplachta.influencerstats

import akka.actor.ActorSystem
import cats.effect.IO
import cats.mtl.FunctorTell
import com.michalplachta.influencerstats.api._
import com.michalplachta.influencerstats.clients.AkkaHttpClient
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.core.model.Collection
import com.michalplachta.influencerstats.logging.IoLogger
import com.michalplachta.influencerstats.state.InMemMapState
import com.typesafe.config.ConfigFactory

object Main extends App {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")

  implicit val system: ActorSystem              = ActorSystem("influencer-stats")
  implicit val logging: FunctorTell[IO, String] = new IoLogger

  val state = new InMemMapState[IO]

  (1 to 10000).map(id => (id.toString, Collection(List.empty))).foreach {
    case (id, collection) =>
      state.saveCollection(id, collection).unsafeRunSync()
  }

  AkkaHttpServer
    .akkaHttpServer(
      host,
      port,
      Statistics.getInfluencerResults(
        state.fetchCollection,
        AkkaHttpClient.getVideoListResponse(youtubeUri, youtubeApiKey)
      ),
      state.fetchCollection,
      state.saveCollection
    )
    .unsafeRunSync()
}
