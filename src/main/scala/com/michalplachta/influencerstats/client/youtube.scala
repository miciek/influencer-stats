package com.michalplachta.influencerstats.client

object youtube {
  final case class VideoListResponse(items: List[Video])

  final case class Video(statistics: VideoStatistics)

  final case class VideoStatistics(viewCount: Int, likeCount: Int)
}
