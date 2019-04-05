package com.michalplachta.influencerstats.java.core;

public class InfluencerItem {
    public final int views;
    public final int likes;
    public final int dislikes;
    public final int comments;

    public InfluencerItem(int views, int likes, int dislikes, int comments) {
        this.views = views;
        this.likes = likes;
        this.dislikes = dislikes;
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "InfluencerItem{" +
                "views=" + views +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                ", comments=" + comments +
                '}';
    }
}
