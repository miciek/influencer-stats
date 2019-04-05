package com.michalplachta.influencerstats.java.logging;

import java.util.concurrent.CompletableFuture;

public interface Logging {
    CompletableFuture<Void> info(String msg);
}
