package com.andipangeran.pusatlawak.actor.api;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Created by jurnal on 2/15/17.
 */
@Value
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class JokeEntity {

    private String type;

    private Joke value;

    @Value
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    static class Joke {

        private int id;

        private String joke;

        private List<String> categories;
    }
}
