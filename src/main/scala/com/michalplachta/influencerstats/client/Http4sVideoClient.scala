package com.michalplachta.influencerstats.client

import cats.effect._
import youtube.VideoListResponse
import io.circe.Decoder
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe._
import org.http4s.client.Client

class Http4sVideoClient(youtubeUri: String, youtubeApiKey: String)(client: Client[IO]) extends VideoClient[IO] {
  def fetchVideoListResponse(videoId: String): IO[VideoListResponse] = {
    implicit def jsonDecoder[F[_]: Sync, A <: Product: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
    client.expect[youtube.VideoListResponse](s"$youtubeUri?part=statistics&id=$videoId&key=$youtubeApiKey")
  }
}
