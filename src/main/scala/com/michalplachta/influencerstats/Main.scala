package com.michalplachta.influencerstats

import akka.actor.ActorSystem
import cats.Monad
import cats.effect.IO
import cats.implicits._
import cats.mtl.FunctorTell
import com.michalplachta.influencerstats.api._
import com.michalplachta.influencerstats.api.youtube.VideoListResponse
import com.michalplachta.influencerstats.clients.AkkaHttpClient
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.core.model.{InfluencerItem, InfluencerResults}
import com.michalplachta.influencerstats.logging.IoLogger
import com.michalplachta.influencerstats.state.InMemListState
import com.typesafe.config.ConfigFactory

object Main extends App {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")

  def getInfluencerResults[F[_]: Monad](
      fetchCollection: String => F[Option[Collection]],
      fetchVideoListResponse: String => F[VideoListResponse]
  )(id: String)(implicit F: FunctorTell[F, String]): F[InfluencerResults] = {
    for {
      _          <- F.tell(s"trying to fetch collection with id $id")
      collection <- fetchCollection(id)
      _          <- F.tell(s"fetched collection: $collection")
      videoIds   = collection.map(_.videos).getOrElse(List.empty)
      _          <- F.tell(s"going to make ${videoIds.size} fetches")
      responses  <- videoIds.map(fetchVideoListResponse).sequence
      _          <- F.tell(s"got responses: $responses")
      items = responses.flatMap(
        _.items.map(_.statistics).map(video => InfluencerItem(video.viewCount, video.likeCount, 0, 0))
      )
      _ <- F.tell(s"got list of influencer items: $items")
    } yield Statistics.calculate(items)
  }

  implicit val system: ActorSystem              = ActorSystem("influencer-stats")
  implicit val logging: FunctorTell[IO, String] = new IoLogger

  val state = new InMemListState[IO]

  (1 to 10000).map(id => (id.toString, Collection(List.empty))).foreach {
    case (id, collection) =>
      state.saveCollection(id, collection).unsafeRunSync()
  }

  AkkaHttpServer
    .akkaHttpServer(
      host,
      port,
      getInfluencerResults(
        state.fetchCollection,
        AkkaHttpClient.getVideoListResponse(youtubeUri, youtubeApiKey)
      ),
      state.fetchCollection,
      state.saveCollection
    )
    .unsafeRunSync()
}
