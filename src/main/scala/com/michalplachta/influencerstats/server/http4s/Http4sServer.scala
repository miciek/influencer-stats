package com.michalplachta.influencerstats.server.http4s

import cats.effect._
import cats.effect.internals.IOContextShift
import com.michalplachta.influencerstats.core.model.{Collection, InfluencerResults}
import com.michalplachta.influencerstats.server.Server
import org.http4s.server.blaze.BlazeBuilder

class Http4sServer extends Server[IO] {
  def serve(host: String,
            port: Int,
            getResults: String => IO[InfluencerResults],
            getCollection: String => IO[Option[Collection]],
            saveCollection: (String, Collection) => IO[Unit]): IO[Unit] = {
    implicit val contextShift: ContextShift[IO] = IOContextShift.global

    val service = new Http4sService(getResults, getCollection, saveCollection)
    BlazeBuilder[IO]
      .bindHttp(port, host)
      .mountService(service.routes, "/")
      .withNio2(isNio2 = true)
      .serve
      .compile
      .drain
  }
}
