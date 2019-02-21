package com.michalplachta.influencerstats.server

import com.michalplachta.influencerstats.core.model.CollectionStats

trait Server[F[_]] {
  def serve(host: String, port: Int, getResults: String => F[CollectionStats]): F[Unit]
}
