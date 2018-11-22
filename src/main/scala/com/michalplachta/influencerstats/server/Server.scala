package com.michalplachta.influencerstats.server

import com.michalplachta.influencerstats.core.model.{Collection, InfluencerResults}
import simulacrum.typeclass

@typeclass
trait Server[F[_]] {
  def serve(host: String,
            port: Int,
            getResults: String => F[InfluencerResults],
            getCollection: String => F[Option[Collection]],
            saveCollection: (String, Collection) => F[Unit]): F[Unit]
}
