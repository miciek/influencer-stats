package com.michalplachta.influencerstats.core
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
}
