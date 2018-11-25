package com.michalplachta.influencerstats

import akka.actor.ActorSystem
import cats.effect.{ContextShift, IO, Timer}
import com.michalplachta.influencerstats.client.{AkkaHttpVideoClient, VideoClient}
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.logging.{DefaultLogger, Logging}
import com.michalplachta.influencerstats.server.Server
import com.michalplachta.influencerstats.server.akkahttp.AkkaHttpServer
import com.michalplachta.influencerstats.state.{AllCollectionsView, CollectionUpdate, CollectionView, InMemListState}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")

  implicit val system: ActorSystem  = ActorSystem("influencer-stats")
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO]     = IO.timer(global)

  implicit val logging: Logging[IO] = new DefaultLogger
  implicit val state: CollectionView[IO] with CollectionUpdate[IO] with AllCollectionsView[IO] =
    new InMemListState
  implicit val client: VideoClient[IO] = new AkkaHttpVideoClient(youtubeUri, youtubeApiKey)
  implicit val server: Server[IO]      = new AkkaHttpServer

  Server[IO]
    .serve(host, port, Statistics.getInfluencerResults[IO])
    .unsafeRunSync()
}
