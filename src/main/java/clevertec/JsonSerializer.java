package clevertec;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonSerializer {

    public String serialize(Object object) throws IllegalAccessException {
        Field[] declaredFields = object.getClass().getDeclaredFields();
        Map<String, Object> elements = new LinkedHashMap<>();
        for (final Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            elements.put(declaredField.getName(), declaredField.get(object));
        }
        return toJson(elements);
    }

    private String toJson(Map<String, Object> elements) {
        String collect = elements.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + "\":" + objecToString(entry.getValue()))
                .collect(Collectors.joining(","));
        return "{" + collect + "}";
    }

    private String objecToString(Object value) {
        if (value == null) {
            return null;
        }
        else if (value instanceof String || value instanceof UUID) {
            return "\"" + value + "\"";
        }
        else if (value instanceof Integer ||
                value instanceof Double ||
                value instanceof Float ||
                value instanceof Byte) {
            return String.valueOf(value);
        }
        else
            return value.toString();
    }

//    public <T> T deserialize(String json, Class<T> clazz) {
//
//    }
}
