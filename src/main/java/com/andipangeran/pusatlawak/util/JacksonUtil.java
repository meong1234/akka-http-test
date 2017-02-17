package com.andipangeran.pusatlawak.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.andipangeran.pusatlawak.config.jackson.ObjectMapperFactory.createDefaultObjectMapper;

/**
 * Created by jurnal on 2/18/17.
 */
public class JacksonUtil {

    private static final ObjectMapper OBJECT_MAPPER = createDefaultObjectMapper();

    public static String toJSON(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot marshal to JSON: " + object, e);
        }
    }
}
