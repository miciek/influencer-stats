package com.michalplachta.influencerstats.clients

import cats.effect._
import hammock.circe.implicits._
import com.michalplachta.influencerstats.api._
import com.michalplachta.influencerstats.api.youtube.VideoListResponse
import hammock._
import hammock.jvm.Interpreter

object HammockClient {
  def getVideoListResponse(youtubeUri: String, youtubeApiKey: String): String => IO[VideoListResponse] = { videoId =>
    import io.circe.generic.auto._
    implicit val interpreter = Interpreter[IO]

    Hammock
      .request(Method.GET, uri"$youtubeUri?part=statistics&id=$videoId&key=$youtubeApiKey", Map.empty)
      .as[youtube.VideoListResponse]
      .exec[IO]
  }
}
