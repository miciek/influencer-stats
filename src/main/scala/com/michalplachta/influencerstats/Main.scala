package com.michalplachta.influencerstats
import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.michalplachta.influencerstats.api.{youtube, Collection, HttpRoutes}
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.core.model.{InfluencerItem, InfluencerResults}
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

object Main extends App {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")

  implicit val system = ActorSystem("influencer-stats")
  implicit val ec     = system.dispatcher

  val state = TrieMap.empty[UUID, Collection]

  def getInfluencerResults(id: UUID): Future[InfluencerResults] = {
    implicit val materializer = ActorMaterializer()

    val youtubeResponses: List[Future[youtube.VideoListResponse]] =
      state.mapValues(_.videos).getOrElse(id, List.empty).map { videoId =>
        Http()
          .singleRequest(HttpRequest(uri = s"$youtubeUri?part=statistics&id=$videoId&key=$youtubeApiKey"))
          .flatMap(Unmarshal(_).to[youtube.VideoListResponse])
      }

    Future
      .sequence(youtubeResponses)
      .map(_.flatMap(_.items.map(_.statistics).map(video => InfluencerItem(video.viewCount, video.likeCount, 0, 0))))
      .map(Statistics.calculate)
  }

  val httpApp = new HttpApp {
    override protected def routes: Route =
      Route.seal(
        HttpRoutes.getInfluencerResults(getInfluencerResults) ~
        HttpRoutes.getCollection(state.get) ~
        HttpRoutes.putCollection(state.put)
      )
  }
  httpApp.startServer(host, port, system)
}
