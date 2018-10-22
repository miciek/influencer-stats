package com.michalplachta.influencerstats

import cats.effect.{ExitCode, IO, IOApp}
import com.michalplachta.influencerstats.api.{youtube, Collection, HttpService}
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.core.model.{InfluencerItem, InfluencerResults}
import com.typesafe.config.ConfigFactory
import hammock.jvm.Interpreter
import hammock._
import io.circe.generic.auto._
import hammock.circe.implicits._
import cats.implicits._
import org.http4s.server.blaze.BlazeBuilder

import scala.collection.concurrent.TrieMap

object Main extends IOApp {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")

  val state = TrieMap.empty[String, Collection]

  def getInfluencerResults(id: String): IO[InfluencerResults] = {
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

  val service = new HttpService(getInfluencerResults, state.get, state.put)

  override def run(args: List[String]): IO[ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(port, host)
      .mountService(service.routes, "/")
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
