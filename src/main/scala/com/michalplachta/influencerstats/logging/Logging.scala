package com.michalplachta.influencerstats.logging
import simulacrum.typeclass

@typeclass
trait Logging[F[_]] {
  def info(msg: String): F[Unit]
}
