package com.andipangeran.pusatlawak.actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.japi.pf.ReceiveBuilder;
import akka.japi.pf.UnitPFBuilder;
import akka.stream.ActorMaterializer;
import com.andipangeran.pusatlawak.actor.api.FetcherCommand;
import com.andipangeran.pusatlawak.actor.api.JokeEntity;
import javaslang.control.Try;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.FiniteDuration;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static akka.japi.pf.ReceiveBuilder.match;
import static akka.pattern.PatternsCS.pipe;
import static com.andipangeran.pusatlawak.util.AkkaUtil.unmarshall;
import static com.andipangeran.pusatlawak.util.FunctionalUtil.toCompleteableTry;

/**
 * Created by jurnal on 2/14/17.
 */
@Slf4j
public class JokesFetcher extends AbstractLoggingActor {

    private static final String JOKES_URL = "http://api.icndb.com/jokes/random?escape=javascript";

    private Optional<Cancellable> cancellable;

    private Http http = Http.get(getContext().system());

    private ActorMaterializer materializer = ActorMaterializer.create(getContext().system());

    @Override
    public void preStart() throws Exception {

        log.debug("Starting up JokeEntity Fetcher");

        akka.actor.Scheduler scheduler = getContext().system().scheduler();

        cancellable = Optional.of(scheduler
            .schedule(
                FiniteDuration.apply(1, TimeUnit.SECONDS),
                FiniteDuration.apply(1, TimeUnit.SECONDS),
                self(),
                new FetcherCommand.FetchJoke(),
                getContext().system().dispatcher(),
                ActorRef.noSender()
            ));
    }

    @Override
    public void postStop() throws Exception {
        cancellable.ifPresent(cancleObj -> cancleObj.cancel());
    }

    public static Props props() {
        return Props.create(JokesFetcher.class, () -> new JokesFetcher());
    }

    private JokesFetcher() {

        UnitPFBuilder<Object> builder = ReceiveBuilder.create();

        receive(ReceiveBuilder
            .match(FetcherCommand.FetchJoke.class, cmd -> {

                log.info("get request for fetching joke");

                pipe(requestJokes(), context().dispatcher()).to(self());

            })
            .match(Try.class, (Try<JokeEntity> cmd) -> {
                log.info("get response {}", cmd);

                cmd.onSuccess(joke -> context().system().eventStream().publish(joke));
            })
            .matchAny(this::unhandled)
            .build()
        );
    }

    private CompletionStage<Try<JokeEntity>> requestJokes() {

        return http
            .singleRequest(HttpRequest.GET(JOKES_URL), materializer)
            .thenCompose(unmarshall(JokeEntity.class, context().dispatcher(), materializer))
            .handle(toCompleteableTry());
    }


}
