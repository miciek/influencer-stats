package com.michalplachta.influencerstats

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits._
import com.michalplachta.influencerstats.api.{youtube, Collection, HttpService}
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.core.model.{InfluencerItem, InfluencerResults}
import com.typesafe.config.ConfigFactory
import io.circe.Decoder
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeBuilder

import scala.collection.concurrent.TrieMap

object Main extends IOApp {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")

  val state = TrieMap.empty[String, Collection]
  val ec    = scala.concurrent.ExecutionContext.global

  def getInfluencerResults(client: Client[IO])(id: String): IO[InfluencerResults] = {
    implicit def jsonDecoder[F[_]: Sync, A <: Product: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]

    val youtubeResponses: IO[List[youtube.VideoListResponse]] =
      state
        .mapValues(_.videos)
        .getOrElse(id, List.empty)
        .map { videoId =>
          client.expect[youtube.VideoListResponse](s"$youtubeUri?part=statistics&id=$videoId&key=$youtubeApiKey")
        }
        .sequence

    youtubeResponses
      .map(_.flatMap(_.items.map(_.statistics).map(video => InfluencerItem(video.viewCount, video.likeCount, 0, 0))))
      .map(Statistics.calculate)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      client  <- BlazeClientBuilder[IO](ec).stream
      service = new HttpService(getInfluencerResults(client), state.get, state.put)
      result <- BlazeBuilder[IO]
                 .bindHttp(port, host)
                 .mountService(service.routes, "/")
                 .withNio2(isNio2 = true)
                 .serve
    } yield result).compile.drain
      .as(ExitCode.Success)
  }
}
