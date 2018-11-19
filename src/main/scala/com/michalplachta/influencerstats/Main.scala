package com.michalplachta.influencerstats

import akka.actor.ActorSystem
import cats.effect.IO
import cats.implicits._
import cats.mtl.{DefaultFunctorTell, FunctorTell}
import cats.{Functor, Monad}
import com.michalplachta.influencerstats.api._
import com.michalplachta.influencerstats.api.youtube.VideoListResponse
import com.michalplachta.influencerstats.clients.AkkaHttpClient
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.core.model.{InfluencerItem, InfluencerResults}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap

object Main extends App {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")
  val state         = TrieMap.empty[String, Collection]

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

  implicit def ioLogger: FunctorTell[IO, String] = new DefaultFunctorTell[IO, String] {
    private val logger = LoggerFactory.getLogger("io-logger")

    override val functor = Functor[IO]
    override def tell(msg: String) = {
      IO {
        logger.info(msg)
      }
    }
  }

  implicit val system: ActorSystem = ActorSystem("influencer-stats")
  AkkaHttpServer
    .akkaHttpServer(
      host,
      port,
      getInfluencerResults(
        id => IO(state.get(id)),
        AkkaHttpClient.getVideoListResponse(youtubeUri, youtubeApiKey)
      ),
      state.get,
      state.put
    )
    .unsafeRunSync()
}
