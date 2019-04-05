package com.michalplachta.influencerstats.java.client;

import java.util.concurrent.CompletableFuture;

public interface VideoClient {
    CompletableFuture<VideoListResponse> fetchVideoListResponse(String videoId);
}
