package com.michalplachta.influencerstats.client

import cats.effect._
import youtube.VideoListResponse
import io.circe.Decoder
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import scala.concurrent.ExecutionContext.Implicits.global

class Http4sVideoClient(youtubeUri: String, youtubeApiKey: String)(implicit contextShift: ContextShift[IO])
    extends VideoClient[IO] {
  def fetchVideoListResponse(videoId: String): IO[VideoListResponse] = {
    implicit def jsonDecoder[F[_]: Sync, A <: Product: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
    BlazeClientBuilder[IO](global).resource.use { client =>
      client.expect[VideoListResponse](s"$youtubeUri?part=statistics&id=$videoId&key=$youtubeApiKey")
    }
  }
}
