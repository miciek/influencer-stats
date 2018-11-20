package com.michalplachta.influencerstats.api

import cats.effect._
import cats.implicits._
import com.michalplachta.influencerstats.core.model.{Collection, InfluencerResults}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeBuilder

object Http4sServer {
  def http4sServer(host: String,
                   port: Int,
                   getResults: Client[IO] => String => IO[InfluencerResults],
                   getCollection: String => IO[Option[Collection]],
                   saveCollection: (String, Collection) => IO[Unit])(implicit F: ConcurrentEffect[IO]): IO[ExitCode] = {
    val ec = scala.concurrent.ExecutionContext.global

    (for {
      client  <- BlazeClientBuilder[IO](ec).stream
      service = new Http4sService(getResults(client), getCollection, saveCollection)
      result <- BlazeBuilder[IO]
                 .bindHttp(port, host)
                 .mountService(service.routes, "/")
                 .withNio2(isNio2 = true)
                 .serve
    } yield result).compile.drain
      .as(ExitCode.Success)
  }
}
