package com.andipangeran.pusatlawak;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import com.andipangeran.pusatlawak.actor.JokePublisher;
import com.andipangeran.pusatlawak.actor.JokesFetcher;
import com.andipangeran.pusatlawak.actor.api.JokeEntity;
import com.andipangeran.pusatlawak.util.JacksonUtil;
import de.heikoseeberger.akkasse.ServerSentEvent;
import de.heikoseeberger.akkasse.japi.EventStreamMarshalling;

import java.util.concurrent.CompletionStage;

/**
 * Created by jurnal on 2/8/17.
 */
public class HttpServer extends AllDirectives {

    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("helloWorld");

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        system.actorOf(JokesFetcher.props(), "jokesFetcher");

        HttpServer app = new HttpServer();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app
            .createRoute()
            .flow(system, materializer);

        final CompletionStage<ServerBinding> binding = http
            .bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8080), materializer);

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return

        binding
            .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
            .thenAccept(unbound -> {

                System.out.println("Server stop");
                system.terminate();
            }); // and shutdown when done
    }

    private Route createRoute() {

        final Source<JokeEntity, ActorRef> source =
            Source.actorPublisher(JokePublisher.props());

        return route(
            path("streaming-jokes", () ->
                get(() -> completeOK(source
                        .map(msg -> ServerSentEvent.create(JacksonUtil.toJSON(msg), "jsonJoke")),
                    EventStreamMarshalling.toEventStream())
                )));
    }

}
