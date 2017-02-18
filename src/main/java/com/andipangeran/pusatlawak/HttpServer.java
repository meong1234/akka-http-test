package com.andipangeran.pusatlawak;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.HttpOrigin;
import akka.http.javadsl.model.headers.HttpOriginRange;
import akka.http.javadsl.server.*;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import ch.megard.akka.http.cors.javadsl.CorsRejection;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;
import com.andipangeran.pusatlawak.actor.JokePublisher;
import com.andipangeran.pusatlawak.actor.JokesFetcher;
import com.andipangeran.pusatlawak.actor.api.Joke;
import com.andipangeran.pusatlawak.actor.api.JokeResponse;
import de.heikoseeberger.akkasse.japi.EventStreamMarshalling;
import de.heikoseeberger.akkasse.japi.ServerSentEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;

/**
 * Created by jurnal on 2/8/17.
 */
@Slf4j
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

        // Your CORS settings
        final CorsSettings settings = CorsSettings.defaultSettings()
            .withAllowedOrigins(HttpOriginRange.create(HttpOrigin.parse("http://localhost:63342")));

        // Your rejection handler
        final RejectionHandler rejectionHandler = RejectionHandler.newBuilder()
            .handle(
                CorsRejection.class,
                msg -> complete(StatusCodes.BAD_REQUEST, "The CORS request is malformed")
            )
            .build();

        // Your exception handler
        final ExceptionHandler exceptionHandler = ExceptionHandler.newBuilder()
            .match(NoSuchElementException.class, ex -> complete(StatusCodes.NOT_FOUND, ex.getMessage()))
            .build();

        // Combining the two handlers only for convenience
        final Function<Supplier<Route>, Route> handleErrors = inner -> Directives.allOf(
            s -> handleExceptions(exceptionHandler, s),
            s -> handleRejections(rejectionHandler, s),
            inner
        );

        List<ServerSentEvent> events =
            Stream.iterate(1, n -> n + 1)
                .limit(666)
                .map(n -> ServerSentEvent.create("" + n))
                .collect(Collectors.toList());

        final Source<Joke, ActorRef> source =
            Source.actorPublisher(JokePublisher.props());


        return handleErrors.apply(() -> cors(settings, () -> handleErrors.apply(() -> route(
            path("streaming-jokes", () ->
                get(() -> completeOK(source.map(msg -> {

                        log.info("streaming-jokes", msg);

                        return ServerSentEvent.create("" + msg.hashCode());

                    }),
                    EventStreamMarshalling.toEventStream())
                ))
        ))));
//        return route(
//            path("streaming-jokes", () ->
//                get(() -> completeOK(source
//                        .map(msg -> ServerSentEvent.create(JacksonUtil.toJSON(msg), "jsonJoke")),
//                    EventStreamMarshalling.toEventStream())
//                )));
    }

}
