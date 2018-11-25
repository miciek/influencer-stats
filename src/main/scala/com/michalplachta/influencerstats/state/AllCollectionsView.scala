package com.michalplachta.influencerstats.state
import simulacrum.typeclass

@typeclass
trait AllCollectionsView[F[_]] {
  def fetchAllCollectionIds: F[List[String]]
}
