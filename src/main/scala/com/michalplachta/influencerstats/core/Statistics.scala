package com.michalplachta.influencerstats.core
import cats.Monad
import cats.implicits._
import cats.mtl.FunctorTell
import com.michalplachta.influencerstats.client.VideoClient
import com.michalplachta.influencerstats.core.model._

object Statistics {
  def calculate(items: List[InfluencerItem]): InfluencerResults = {
    items.foldLeft(InfluencerResults(0, 0, 0)) { (results, item) =>
      InfluencerResults(
        impressions = results.impressions + item.views,
        engagements = results.engagements + item.comments + item.likes + item.dislikes,
        score = results.engagements + item.likes
      )
    }
  }

  def getInfluencerResults[F[_]: Monad: VideoClient](
      fetchCollection: String => F[Option[Collection]]
  )(id: String)(implicit F: FunctorTell[F, String]): F[InfluencerResults] = {
    for {
      _          <- F.tell(s"trying to fetch collection with id $id")
      collection <- fetchCollection(id)
      _          <- F.tell(s"fetched collection: $collection")
      videoIds   = collection.map(_.videos).getOrElse(List.empty)
      _          <- F.tell(s"going to make ${videoIds.size} fetches")
      responses  <- videoIds.map(VideoClient[F].fetchVideoListResponse).sequence
      _          <- F.tell(s"got responses: $responses")
      items = responses.flatMap(
        _.items.map(_.statistics).map(video => InfluencerItem(video.viewCount, video.likeCount, 0, 0))
      )
      _ <- F.tell(s"got list of influencer items: $items")
    } yield calculate(items)
  }

}
