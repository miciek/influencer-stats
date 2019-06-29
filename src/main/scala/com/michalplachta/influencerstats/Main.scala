package com.michalplachta.influencerstats

import akka.actor.ActorSystem
import cats.effect.IO
import com.michalplachta.influencerstats.client._
import com.michalplachta.influencerstats.core.Statistics
import com.michalplachta.influencerstats.logging._
import com.michalplachta.influencerstats.server.akkahttp.AkkaHttpServer
import com.michalplachta.influencerstats.state._
import com.typesafe.config.ConfigFactory

object Main extends App {
  val config        = ConfigFactory.load()
  val host          = config.getString("app.host")
  val port          = config.getInt("app.port")
  val youtubeUri    = config.getString("apis.youtubeUri")
  val youtubeApiKey = config.getString("apis.youtubeApiKey")

}
