package com.michalplachta.influencerstats.java.client;

public class VideoStatistics {
    public final int viewCount;
    public final int likeCount;

    public VideoStatistics(int viewCount, int likeCount) {
        this.viewCount = viewCount;
        this.likeCount = likeCount;
    }

    @Override
    public String toString() {
        return "VideoStatistics{" +
                "viewCount=" + viewCount + "," +
                "likeCount=" + likeCount +
                '}';
    }

}
