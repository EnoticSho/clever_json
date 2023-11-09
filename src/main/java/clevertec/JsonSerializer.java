package clevertec;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonSerializer {

    public String serialize(Object object) {
        Field[] declaredFields = object.getClass().getDeclaredFields();
        Map<String, Object> elements = new LinkedHashMap<>();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            try {
                elements.put(declaredField.getName(), declaredField.get(object));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return toJson(elements);
    }

    private String toJson(Map<String, Object> elements) {
        String collect = elements.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + "\":" + objectToString(entry.getValue()))
                .collect(Collectors.joining(","));
        return "{" + collect + "}";
    }

    private String objectToString(Object value) {
        if (value == null) {
            return "null";
        }
        else if (value instanceof String ||
                value instanceof UUID) {
            return "\"" + value + "\"";
        }
        else if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        else if (value instanceof OffsetDateTime) {
            return "\"" + value + "\"";
        }
        else if (value instanceof LocalDate) {
            return "\"" + value + "\"";
        }
        else if (value.getClass().isArray()) {
            return arrayToJson(value);
        }
        else if (value instanceof Collection<?>) {
            return collectionsToJson((Collection<?>) value);
        }
        else if (value instanceof Map<?,?>) {
            return mapToJson((Map<?, ?>) value);
        }
        else {
            return serialize(value);
        }
    }

    private String arrayToJson(Object array) {
        List<String> arrayElements = new ArrayList<>();
        for (int i = 0; i < Array.getLength(array); i++) {
            Object element = Array.get(array, i);
            if (!element.getClass().isArray() && !element.getClass().isPrimitive() && !(element instanceof String)) {
                arrayElements.add(serialize(element));
            }
            else {
                arrayElements.add(objectToString(element));
            }
        }
        return "[" + String.join(",", arrayElements) + "]";
    }

    private String collectionsToJson(Collection<?> value) {
        return "[" + value.stream()
                .map(this::objectToString)
                .collect(Collectors.joining(",")) + "]";
    }

    private String mapToJson(Map<?, ?> value) {
        return "{" + value.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + "\":" + objectToString(entry.getValue()))
                .collect(Collectors.joining(",")) + "}";
    }

    public <T> T deserialize(String json, Class<T> clazz) {

        return null;
    }
}
