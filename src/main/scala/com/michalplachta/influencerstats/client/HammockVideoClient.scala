package com.michalplachta.influencerstats.client

import cats.data.NonEmptyList
import cats.effect._
import com.michalplachta.influencerstats.client.youtube.VideoListResponse
import hammock._
import hammock.circe.implicits._
import hammock.jvm.Interpreter
import io.circe.generic.auto._

class HammockVideoClient(youtubeUri: String, youtubeApiKey: String) extends VideoClient[IO] {
  implicit val interpreter: Interpreter[IO] = Interpreter[IO]
  private val baseUri                       = uri"$youtubeUri?part=statistics&key=$youtubeApiKey"

  def fetchVideoListResponse(videoId: String): IO[VideoListResponse] = {
    Hammock
      .request(Method.GET, baseUri ? NonEmptyList.of("videoId" -> videoId), Map.empty)
      .as[youtube.VideoListResponse]
      .exec[IO]
  }
}
