package com.michalplachta.influencerstats.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cats.effect._
import com.michalplachta.influencerstats.client.youtube.VideoListResponse
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContextExecutor

class AkkaHttpVideoClient(youtubeUri: String, youtubeApiKey: String)(implicit system: ActorSystem)
    extends VideoClient[IO] {
  def fetchVideoListResponse(videoId: String): IO[VideoListResponse] = {
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor    = system.dispatcher

    IO.fromFuture(IO {
      Http()
        .singleRequest(HttpRequest(uri = s"$youtubeUri?part=statistics&id=$videoId&key=$youtubeApiKey"))
        .flatMap(Unmarshal(_).to[youtube.VideoListResponse])
    })
  }
}
