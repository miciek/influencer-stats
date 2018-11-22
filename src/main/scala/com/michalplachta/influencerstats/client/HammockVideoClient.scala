package com.michalplachta.influencerstats.client

import cats.effect._
import com.michalplachta.influencerstats.client.youtube.VideoListResponse
import hammock._
import hammock.circe.implicits._
import hammock.jvm.Interpreter

class HammockVideoClient(youtubeUri: String, youtubeApiKey: String) extends VideoClient[IO] {
  def fetchVideoListResponse(videoId: String): IO[VideoListResponse] = {
    import io.circe.generic.auto._
    implicit val interpreter: Interpreter[IO] = Interpreter[IO]

    Hammock
      .request(Method.GET, uri"$youtubeUri?part=statistics&id=$videoId&key=$youtubeApiKey", Map.empty)
      .as[youtube.VideoListResponse]
      .exec[IO]
  }
}
