package com.michalplachta.influencerstats.java.client;

import java.util.List;

public class VideoListResponse {
    public final List<Video> items;

    public VideoListResponse(List<Video> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "VideoListResponse{" +
                "items=" + items +
                '}';
    }
}
