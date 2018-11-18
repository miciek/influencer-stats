package com.michalplachta.influencerstats

import akka.actor.ActorSystem
import cats.effect.IO
import cats.implicits._
import cats.mtl.{DefaultFunctorTell, FunctorTell}
import cats.{Functor, Monad}
import com.michalplachta.influencerstats.api._
import com.michalplachta.influencerstats.api.youtube.VideoListResponse
import com.michalplachta.influencerstats.clients.AkkaHttpClient
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.core.model.{InfluencerItem, InfluencerResults}
import com.typesafe.config.ConfigFactory

import scala.collection.concurrent.TrieMap

object Main extends App {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")
  val state         = TrieMap.empty[String, Collection]

  def getInfluencerResults[F[_]: Monad](
      getVideoListResponse: String => F[VideoListResponse]
  )(id: String)(implicit F: FunctorTell[F, String]): F[InfluencerResults] = {
    val youtubeResponses: F[List[youtube.VideoListResponse]] =
      state
        .mapValues(_.videos)
        .getOrElse(id, List.empty)
        .map(getVideoListResponse)
        .sequence

    youtubeResponses
      .flatMap { responses =>
        F.tell(s"got responses: $responses").map(_ => responses)
      }
      .map(_.flatMap(_.items.map(_.statistics).map(video => InfluencerItem(video.viewCount, video.likeCount, 0, 0))))
      .flatMap { items =>
        F.tell(s"got list of influencer items: $items").map(_ => Statistics.calculate(items))
      }
  }

  implicit def ioLogger: FunctorTell[IO, String] = new DefaultFunctorTell[IO, String] {
    override val functor = Functor[IO]
    override def tell(l: String) = {
      IO {
        println(s"LOG: $l")
        println(s"LOG: $l")
      }
    }
  }

  implicit val system: ActorSystem = ActorSystem("influencer-stats")
  AkkaHttpServer
    .akkaHttpServer(
      host,
      port,
      getInfluencerResults(AkkaHttpClient.getVideoListResponse(youtubeUri, youtubeApiKey)),
      state.get,
      state.put
    )
    .unsafeRunSync()
}
