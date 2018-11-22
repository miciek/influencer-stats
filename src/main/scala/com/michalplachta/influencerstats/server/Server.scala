package com.michalplachta.influencerstats.server

import com.michalplachta.influencerstats.core.model.InfluencerResults
import simulacrum.typeclass

@typeclass
trait Server[F[_]] {
  def serve(host: String, port: Int, getResults: String => F[InfluencerResults]): F[Unit]
}
