package com.michalplachta.influencerstats.logging
import simulacrum.typeclass

@typeclass
trait Logging[F[_]] {
  def debug(msg: String): F[Unit]
}
