package com.michalplachta.influencerstats.server

import com.michalplachta.influencerstats.core.model.CollectionStats
import simulacrum.typeclass

@typeclass
trait Server[F[_]] {
  def serve(host: String, port: Int, getResults: String => F[CollectionStats]): F[Unit]
}
