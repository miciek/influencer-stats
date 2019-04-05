package com.michalplachta.influencerstats.java.state;

import com.michalplachta.influencerstats.java.core.Collection;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CollectionView {
    CompletableFuture<Optional<Collection>> fetchCollection(String id);
}
