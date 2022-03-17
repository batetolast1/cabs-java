package io.legacyfighter.cabs.entity.miles;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

final class MilesJsonMapper {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    private MilesJsonMapper() {
    }

    static Miles deserialize(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Miles.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static String serialize(Miles miles) {
        try {
            return OBJECT_MAPPER.writeValueAsString(miles);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
