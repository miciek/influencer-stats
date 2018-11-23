package com.michalplachta.influencerstats.core

object model {
  final case class Collection(videos: List[String])

  final case class InfluencerItem(views: Int, likes: Int, dislikes: Int, comments: Int)

  final case class InfluencerResults(impressions: Int, engagements: Int, score: Int)

  object InfluencerResults {
    def empty = InfluencerResults(0, 0, 0)
  }
}
