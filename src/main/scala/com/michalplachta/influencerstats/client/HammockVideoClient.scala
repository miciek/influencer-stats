package com.michalplachta.influencerstats.client

import cats.data.NonEmptyList
import cats.effect._
import com.michalplachta.influencerstats.client.youtube.VideoListResponse
import hammock._
import hammock.circe.implicits._
import hammock.jvm.Interpreter
import io.circe.generic.auto._
