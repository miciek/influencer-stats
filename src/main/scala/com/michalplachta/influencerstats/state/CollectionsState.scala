package com.michalplachta.influencerstats.state
import com.michalplachta.influencerstats.core.model.Collection
import simulacrum.typeclass

@typeclass
trait CollectionsState[F[_]] {
  def fetchCollection(id: String): F[Option[Collection]]

  def saveCollection(id: String, collection: Collection): F[Unit]
}
