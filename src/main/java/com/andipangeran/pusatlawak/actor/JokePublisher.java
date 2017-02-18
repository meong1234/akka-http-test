package com.andipangeran.pusatlawak.actor;

import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.actor.AbstractActorPublisher;
import akka.stream.actor.ActorPublisherMessage;
import com.andipangeran.pusatlawak.actor.api.Joke;
import com.andipangeran.pusatlawak.actor.api.JokeResponse;
import javaslang.Tuple2;
import javaslang.collection.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by jurnal on 2/17/17.
 */
@Slf4j
public class JokePublisher extends AbstractActorPublisher<Joke> {

    private final int MAX_BUFFER_SIZE = 100;
    private List<Joke> buf = List.empty();

    public static Props props() {
        return Props.create(JokePublisher.class, () -> new JokePublisher());
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.debug("Starting up JokePublisher {}", getContext().system().eventStream().toString());
        getContext().system().eventStream().subscribe(self(), Joke.class);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log.debug("Stop JokePublisher");
        getContext().system().eventStream().unsubscribe(self());
    }

    public JokePublisher() {

        receive(ReceiveBuilder
            .match(Joke.class, msg -> buf.size() == MAX_BUFFER_SIZE, msg -> {
                log.warn("Buffer is full, ignoring incoming JokeEvent");
            })
            .match(Joke.class, msg -> {

                log.info("get request for publish joke");

                if (buf.isEmpty() && totalDemand() > 0)
                    onNext(msg);
                else {
                    buf = buf.append(msg);
                    deliverBuf();
                }
            })
            .match(ActorPublisherMessage.Request.class, request -> deliverBuf())
            .match(ActorPublisherMessage.Cancel.class, cancel -> context().stop(self()))
            .matchAny(msg -> {
                log.info("JokePublisher recieved {}", msg);
            })
            .build()
        );
    }

    private void deliverBuf() {

        while (totalDemand() > 0) {
            if (totalDemand() <= Integer.MAX_VALUE) {
                nextBuff((int) totalDemand());
            } else {
                nextBuff(Integer.MAX_VALUE);
            }
        }
    }

    private void nextBuff(int demand) {
        Tuple2<List<Joke>, List<Joke>> tuple2 = this.buf.splitAt(demand);
        this.buf = tuple2._2;
        tuple2._1.forEach(this::onNext);
    }


}
