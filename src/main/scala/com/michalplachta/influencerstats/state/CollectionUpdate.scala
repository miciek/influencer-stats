package com.michalplachta.influencerstats.state

import com.michalplachta.influencerstats.core.model.Collection
import simulacrum.typeclass

@typeclass
trait CollectionUpdate[F[_]] {
  def saveCollection(id: String, collection: Collection): F[Unit]
}
