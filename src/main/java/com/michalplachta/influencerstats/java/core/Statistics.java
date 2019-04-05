package com.michalplachta.influencerstats.java.core;

import com.michalplachta.influencerstats.java.client.VideoClient;
import com.michalplachta.influencerstats.java.client.VideoListResponse;
import com.michalplachta.influencerstats.java.logging.Logging;
import com.michalplachta.influencerstats.java.state.CollectionView;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

public class Statistics {
    private CollectionView collectionView;
    private VideoClient videoClient;
    private Logging logging;

    public Statistics(CollectionView collectionView, VideoClient videoClient, Logging logging) {
        this.collectionView = collectionView;
        this.videoClient = videoClient;
        this.logging = logging;
    }

    public CompletableFuture<CollectionStats> getStats(String collectionId) {
        return logging.info("trying to fetch collection with id " + collectionId)
                .thenCompose(ignored ->
                        collectionView.fetchCollection(collectionId)
                )
                .thenCompose(collection -> {
                    logging.info("fetched collection: " + collection);
                    List<String> videoIds = collection.map(c -> c.videos).orElse(emptyList());
                    logging.info("going to make " + videoIds.size() + " fetches");
                    return sequence(videoIds
                            .stream()
                            .map(videoClient::fetchVideoListResponse).collect(toList()));
                })
                .thenApply(responses -> {
                    logging.info("got responses: " + responses);
                    List<InfluencerItem> items = responses.stream().flatMap(r -> responseToItems(r).stream()).collect(toList());
                    logging.info("got list of influencer items: " + items);
                    return calculate(items);
                });
    }

    private static CollectionStats calculate(List<InfluencerItem> items) {
        return items.stream().reduce(new CollectionStats(0, 0, 0), (results, item) ->
                        new CollectionStats(
                                results.impressions + item.views,
                                results.engagements + item.comments + item.likes + item.dislikes,
                                results.engagements + item.likes
                        ), (a, b) -> new CollectionStats(a.impressions + b.impressions, a.engagements + b.engagements, a.score + b.score)
                );
    }

    private static List<InfluencerItem> responseToItems(VideoListResponse response) {
        return response.items
                .stream()
                .map(v -> v.statistics)
                .map(video -> new InfluencerItem(video.viewCount, video.likeCount, 0, 0))
                .collect(Collectors.toList());
    }

    private static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        return futures.stream()
                .map(f -> f.thenApply(Stream::of))
                .reduce((a, b) -> a.thenCompose(xs -> b.thenApply(ys -> concat(xs, ys))))
                .map(f -> f.thenApply(s -> s.collect(toList())))
                .orElse(completedFuture(emptyList()));
    }
}
