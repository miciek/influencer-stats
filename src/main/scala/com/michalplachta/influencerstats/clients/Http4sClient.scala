package com.michalplachta.influencerstats.clients

import cats.effect._
import com.michalplachta.influencerstats.api._
import com.michalplachta.influencerstats.api.youtube.VideoListResponse
import io.circe.Decoder
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe._
import org.http4s.client.Client

object Http4sClient {
  def getVideoListResponse(client: Client[IO], youtubeUri: String, youtubeApiKey: String)(
      videoId: String
  ): IO[VideoListResponse] = {
    implicit def jsonDecoder[F[_]: Sync, A <: Product: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
    client.expect[youtube.VideoListResponse](s"$youtubeUri?part=statistics&id=$videoId&key=$youtubeApiKey")
  }
}
