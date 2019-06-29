package com.michalplachta.influencerstats.core

import cats.Monad
import cats.implicits._
import com.michalplachta.influencerstats.client.VideoClient
import com.michalplachta.influencerstats.client.youtube.VideoListResponse
import com.michalplachta.influencerstats.core.model._
import com.michalplachta.influencerstats.logging.Logging
import com.michalplachta.influencerstats.state.CollectionView

object Statistics {
  def calculate(items: List[InfluencerItem]): CollectionStats = {
    items.foldLeft(CollectionStats.empty) { (results, item) =>
      CollectionStats(
        impressions = results.impressions + item.views,
        engagements = results.engagements + item.comments + item.likes + item.dislikes,
        score = results.engagements + item.likes
      )
    }
  }

  def responseToItems(response: VideoListResponse): List[InfluencerItem] = {
    response.items.map(_.statistics).map(video => InfluencerItem(video.viewCount, video.likeCount, 0, 0))
  }

  def getStats[F[_]: Monad: CollectionView: VideoClient: Logging](collectionId: String): F[CollectionStats] = {
    for {
      _          <- Logging[F].info(s"trying to fetch collection with id $collectionId")
      collection <- CollectionView[F].fetchCollection(collectionId)
      _          <- Logging[F].info(s"fetched collection: $collection")
      videoIds   = collection.map(_.videos).getOrElse(List.empty)
      _          <- Logging[F].info(s"going to make ${videoIds.size} fetches")
      responses  <- videoIds.map(VideoClient[F].fetchVideoListResponse).sequence
      _          <- Logging[F].info(s"got responses: $responses")
      items      = responses.flatMap(responseToItems)
      _          <- Logging[F].info(s"got list of influencer items: $items")
    } yield calculate(items)
  }
}
