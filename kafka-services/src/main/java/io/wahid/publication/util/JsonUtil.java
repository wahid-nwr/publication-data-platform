package io.wahid.publication.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private JsonUtil() {
        try {
            // avoid timestamps like 1694304000000, use ISO strings instead
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to bytes", e);
        }
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static <T> T fromBytes(byte[] data, Class<T> clazz) throws Exception {
        return mapper.readValue(data, clazz);
    }

    public static byte[] toBytes(Object obj) {
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to bytes", e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}
