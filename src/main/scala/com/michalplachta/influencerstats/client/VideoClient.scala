package com.michalplachta.influencerstats.client

import com.michalplachta.influencerstats.client.youtube.VideoListResponse
import simulacrum.typeclass

@typeclass
trait VideoClient[F[_]] {
  def fetchVideoListResponse(videoId: String): F[VideoListResponse]
}
