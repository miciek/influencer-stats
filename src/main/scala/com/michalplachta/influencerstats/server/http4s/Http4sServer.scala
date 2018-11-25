package com.michalplachta.influencerstats.server.http4s

import cats.Monad
import cats.effect._
import com.michalplachta.influencerstats.core.model.InfluencerResults
import com.michalplachta.influencerstats.server.Server
import com.michalplachta.influencerstats.state.{CollectionUpdate, CollectionView}
import org.http4s.server.blaze.BlazeBuilder

class Http4sServer[F[_]: Monad: Sync: ConcurrentEffect: Timer: CollectionView: CollectionUpdate] extends Server[F] {
  def serve(host: String, port: Int, getResults: String => F[InfluencerResults]): F[Unit] = {

    val service = new Http4sService(getResults)
    BlazeBuilder[F]
      .bindHttp(port, host)
      .mountService(service.routes, "/")
      .withNio2(isNio2 = true)
      .serve
      .compile
      .drain
  }
}
