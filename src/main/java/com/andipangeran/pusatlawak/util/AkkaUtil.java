package com.andipangeran.pusatlawak.util;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.stream.Materializer;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.ExecutionContext;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller;

/**
 * Created by jurnal on 2/15/17.
 */
@Slf4j
public class AkkaUtil {

    public static <T> Function<HttpResponse, CompletionStage<T>> unmarshall(
        Class<T> expectedType, ExecutionContext ec, Materializer materializer) {

        return httpResponse -> unmarshaller(expectedType)
            .unmarshall(httpResponse.entity(), ec, materializer);
    }

    public static Function<HttpResponse, CompletionStage<String>> stringUnmarshall(
        ExecutionContext ec, Materializer materializer) {

        return httpResponse -> Unmarshaller
            .entityToString()
            .unmarshall(httpResponse.entity(), ec, materializer);
    }

}
