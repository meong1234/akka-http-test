package com.andipangeran.pusatlawak.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

import static com.andipangeran.pusatlawak.util.PresentUtil.isPresent;

/**
 * Created by jurnal on 2/14/17.
 */
@Slf4j
public class FunctionalUtil {

    public static <T> BiConsumer<T, Throwable>
    logItemOrError(String processName) {
        return (item, throwable) -> {
            if (isPresent(throwable)) {
                log.error(" error on {} {}", processName, throwable.getMessage());
            } else {
                log.info(" capture on {} {}", processName, item);
            }
        };
    }
}
