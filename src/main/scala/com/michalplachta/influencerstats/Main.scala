package com.michalplachta.influencerstats

import akka.actor.ActorSystem
import cats.effect.IO
import com.michalplachta.influencerstats.client._
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.logging._
import com.michalplachta.influencerstats.server.akkahttp.AkkaHttpServer
import com.michalplachta.influencerstats.state._
import com.typesafe.config.ConfigFactory

object Main extends App {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")

  implicit val system: ActorSystem = ActorSystem("influencer-stats")

  implicit val logging: Logging[IO]    = new DefaultLogger
  implicit val client: VideoClient[IO] = new AkkaHttpVideoClient(youtubeUri, youtubeApiKey)
  implicit val collections: CollectionView[IO] with CollectionUpdate[IO] with AllCollectionsView[IO] =
    new InMemListState

  new AkkaHttpServer(host, port)
    .serve(Statistics.getStats[IO])
    .unsafeRunSync()
}
