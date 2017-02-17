package com.andipangeran.pusatlawak.util;

import javaslang.control.Try;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.andipangeran.pusatlawak.util.PresentUtil.isPresent;

/**
 * Created by jurnal on 2/14/17.
 */
@Slf4j
public class FunctionalUtil {

    public static <T> BiConsumer<T, Throwable> logItemOrError(String processName) {
        return (item, throwable) -> {
            if (isPresent(throwable)) {
                log.error(" error on {} {}", processName, throwable.getMessage());
            } else {
                log.info(" capture on {} {}", processName, item);
            }
        };
    }

    public static <T> BiFunction<T, Throwable, Try<T>> toCompleteableTry() {
        return (item, throwable) -> (isPresent(throwable))
            ? Try.failure(throwable)
            : Try.success(item);
    }
}
