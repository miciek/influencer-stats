package com.michalplachta.influencerstats.core

object model {
  final case class InfluencerItem(views: Int, likes: Int, dislikes: Int, comments: Int)

  final case class InfluencerResults(impressions: Int, engagements: Int, score: Int)
}
