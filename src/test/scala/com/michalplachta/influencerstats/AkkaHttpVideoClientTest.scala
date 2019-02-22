package com.michalplachta.influencerstats

import akka.actor.ActorSystem
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import com.michalplachta.influencerstats.client.AkkaHttpVideoClient
import com.michalplachta.influencerstats.client.youtube.{Video, VideoListResponse, VideoStatistics}
import org.scalatest.{Matchers, WordSpec}
import org.testcontainers.containers.wait.strategy.Wait

class AkkaHttpVideoClientTest extends WordSpec with Matchers with ForAllTestContainer {
  override val container =
    GenericContainer("miciek/influencer-stats-youtube:v1", exposedPorts = Seq(80), waitStrategy = Wait.forListeningPort)

  "AkkaHttpVideoClient" should {
    "be able to get and parse response from Youtube API" in new WithYoutubeServer {
      val videoClient: AkkaHttpVideoClient = new AkkaHttpVideoClient(youtubeUri, "test")(ActorSystem("test"))

      val result = videoClient.fetchVideoListResponse("-4lB5EKS5Uk").unsafeRunSync()
      result should be(
        VideoListResponse(
          List(
            Video(VideoStatistics(viewCount = 261734, likeCount = 14237))
          )
        )
      )
    }

  }

  trait WithYoutubeServer {
    lazy val youtubeUri = s"http://${container.containerIpAddress}:${container.mappedPort(80)}/youtube/v3/videos"
  }
}
