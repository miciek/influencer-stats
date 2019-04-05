package com.michalplachta.influencerstats.java.client;

public class Video {
    public final VideoStatistics statistics;

    public Video(VideoStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public String toString() {
        return "Video{" +
                "statistics=" + statistics +
                '}';
    }
}
