package com.michalplachta.influencerstats.java;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.michalplachta.influencerstats.java.client.MockVideoClient;
import com.michalplachta.influencerstats.java.core.CollectionStats;
import com.michalplachta.influencerstats.java.core.Statistics;
import com.michalplachta.influencerstats.java.logging.ConsoleLogging;
import com.michalplachta.influencerstats.java.server.akkahttp.AkkaHttpRoutes;
import com.michalplachta.influencerstats.java.state.MockCollectionView;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import static akka.http.javadsl.server.PathMatchers.segment;

/**
 * NOTE that this is not a full implementation of the Influencer Stats app.
 * This is not intended to be comparable with Scala solution as a whole.
 * This is just for presentation purpose of Statistics class code. DO NOT analyse the implementation. It's not correct.
 *
 * Just Statistics class should be compared. The rest of the Java version is mocked.
 *
 * @see com.michalplachta.influencerstats.java.core.Statistics
 */
public class MainJava extends AllDirectives {
    private Statistics statistics = new Statistics(new MockCollectionView(), new MockVideoClient(), new ConsoleLogging());

    public static void main(String[] args) throws IOException {
        Config config        = ConfigFactory.load();
        String host          = config.getString("app.host");
        int port          = config.getInt("app.port");

        ActorSystem system = ActorSystem.create("influencer-stats");

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = new MainJava().createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost(host, port), materializer);

        System.out.println("Server online\nPress RETURN to stop...");
        System.in.read();

        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }

    private Route createRoute() {
        return route(
                path(segment("collections").slash(segment()).slash("stats"), collectionId ->
                        get(() -> completeWithFuture(statistics.getStats(collectionId).thenApply(s -> HttpResponse.create().withEntity(toJson(s)))))));
    }

    private static String toJson(CollectionStats s) {
        return "{ \n"
                + "  \"impressions\": " + s.impressions + ",\n"
                + "  \"engagements\": " + s.engagements + ",\n"
                + "  \"score\": " + s.score + ",\n"
                + "}";
    }
}
