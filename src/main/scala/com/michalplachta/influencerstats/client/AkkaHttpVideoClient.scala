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

import scala.concurrent.ExecutionContext

class AkkaHttpVideoClient(youtubeUri: String, youtubeApiKey: String)(implicit system: ActorSystem)
    extends VideoClient[IO] {
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext            = system.dispatcher

  def fetchVideoListResponse(videoId: String): IO[VideoListResponse] = {
    IO.fromFuture(IO {
      Http()
        .singleRequest(HttpRequest(uri = s"$youtubeUri?part=statistics&id=$videoId&key=$youtubeApiKey"))
        .flatMap(Unmarshal(_).to[youtube.VideoListResponse])
    })
  }
}
