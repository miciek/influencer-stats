package com.michalplachta.influencerstats.java.client;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * NOTE: This is just for presentation purpose of Statistics class code. DO NOT analyse the implementation. It's not correct.
 *
 * @see com.michalplachta.influencerstats.java.core.Statistics
 */
public class MockVideoClient implements VideoClient {
    @Override
    public CompletableFuture<VideoListResponse> fetchVideoListResponse(String videoId) {
        CompletableFuture<VideoListResponse> f = new CompletableFuture<>();
        f.complete(new VideoListResponse(Collections.singletonList(new Video(new VideoStatistics(721, 2)))));
        return f;
    }
}
