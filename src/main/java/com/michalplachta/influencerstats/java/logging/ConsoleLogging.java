package com.michalplachta.influencerstats.java.logging;

import java.util.concurrent.CompletableFuture;

/**
 * NOTE: This is just for presentation purpose of Statistics class code. DO NOT analyse the implementation. It's not correct.
 *
 * @see com.michalplachta.influencerstats.java.core.Statistics
 */
public class ConsoleLogging implements Logging {
    @Override
    public CompletableFuture<Void> info(String msg) {
        CompletableFuture<Void> f = new CompletableFuture<>();
        System.out.println(msg); // should be in another thread
        f.complete(null);
        return f;
    }
}
