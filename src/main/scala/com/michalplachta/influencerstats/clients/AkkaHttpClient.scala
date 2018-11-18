package com.michalplachta.influencerstats.clients

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cats.effect._
import com.michalplachta.influencerstats.api._
import com.michalplachta.influencerstats.api.youtube.VideoListResponse

object AkkaHttpClient {
  def getVideoListResponse(youtubeUri: String, youtubeApiKey: String)(
      implicit system: ActorSystem
  ): String => IO[VideoListResponse] = { videoId =>
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
    import io.circe.generic.auto._

    implicit val materializer = ActorMaterializer()
    implicit val ec           = system.dispatcher

    IO.fromFuture(IO {
      Http()
        .singleRequest(HttpRequest(uri = s"$youtubeUri?part=statistics&id=$videoId&key=$youtubeApiKey"))
        .flatMap(Unmarshal(_).to[youtube.VideoListResponse])
    })
  }
}
