package com.michalplachta.influencerstats
import java.util.UUID

import akka.http.scaladsl.server.{HttpApp, Route}
import cats.effect.IO
import com.michalplachta.influencerstats.api.{youtube, Collection, HttpRoutes}
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.core.model.{InfluencerItem, InfluencerResults}
import com.typesafe.config.ConfigFactory
import hammock.jvm.Interpreter
import hammock._
import io.circe.generic.auto._
import hammock.circe.implicits._

import cats.implicits._

import scala.collection.concurrent.TrieMap

object Main extends App {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")

  val state = TrieMap.empty[UUID, Collection]

  def getInfluencerResults(id: UUID): IO[InfluencerResults] = {
    implicit val interpreter = Interpreter[IO]

    val youtubeResponses: IO[List[youtube.VideoListResponse]] =
      state
        .mapValues(_.videos)
        .getOrElse(id, List.empty)
        .map { videoId =>
          Hammock
            .request(Method.GET, uri"$youtubeUri?part=statistics&id=$videoId&key=$youtubeApiKey", Map.empty)
            .as[youtube.VideoListResponse]
            .exec[IO]
        }
        .sequence

    youtubeResponses
      .map(_.flatMap(_.items.map(_.statistics).map(video => InfluencerItem(video.viewCount, video.likeCount, 0, 0))))
      .map(Statistics.calculate)
  }

  val httpApp = new HttpApp {
    override protected def routes: Route =
      Route.seal(
        HttpRoutes.getInfluencerResults(getInfluencerResults) ~
        HttpRoutes.getCollection(state.get) ~
        HttpRoutes.putCollection(state.put)
      )
  }
  httpApp.startServer(host, port)
}
