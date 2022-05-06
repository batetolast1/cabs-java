package io.legacyfighter.cabs.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TariffJsonMapper {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    private TariffJsonMapper() {
    }

    public static Tariff deserialize(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Tariff.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String serialize(Tariff tariff) {
        try {
            return OBJECT_MAPPER.writeValueAsString(tariff);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
