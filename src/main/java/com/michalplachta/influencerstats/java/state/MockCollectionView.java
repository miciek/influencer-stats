package com.michalplachta.influencerstats.java.state;

import com.michalplachta.influencerstats.java.core.Collection;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * NOTE: This is just for presentation purpose of Statistics class code. DO NOT analyse the implementation. It's not correct.
 *
 * @see com.michalplachta.influencerstats.java.core.Statistics
 */
public class MockCollectionView implements CollectionView {
    @Override
    public CompletableFuture<Optional<Collection>> fetchCollection(String id) {
        CompletableFuture<Optional<Collection>> f = new CompletableFuture<>();
        f.complete(Optional.of(new Collection(Arrays.asList("mockId1", "mockId2"))));
        return f;
    }
}
