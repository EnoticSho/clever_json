package clevertec;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonSerializer {

    private static final String QUOTE = "\"";
    private static final String COLON = ":";
    private static final String COMMA = ",";
    private static final String NULL = "null";
    private static final String OPEN_BRACE = "{";
    private static final String CLOSE_BRACE = "}";
    private static final String OPEN_BRACKET = "[";
    private static final String CLOSE_BRACKET = "]";

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
                .map(entry -> QUOTE + entry.getKey() + QUOTE + COLON + objectToString(entry.getValue()))
                .collect(Collectors.joining(COMMA));
        return OPEN_BRACE + collect + CLOSE_BRACE;
    }

    private String objectToString(Object value) {
        if (value == null) {
            return NULL;
        }
        else if (value instanceof String ||
                value instanceof UUID ||
                value instanceof OffsetDateTime ||
                value instanceof LocalDate
        ) {
            return QUOTE + value + QUOTE;
        }
        else if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        else if (isPrimitiveOrWrapper(value)) {
            return String.valueOf(value);
        }
        else if (value.getClass().isArray()) {
            return arrayToJson(value);
        }
        else if (value instanceof Collection<?>) {
            return collectionsToJson((Collection<?>) value);
        }
        else if (value instanceof Map<?, ?>) {
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
            if (!isPrimitiveOrWrapper(element) && !(element instanceof String)) {
                arrayElements.add(serialize(element));
            }
            else {
                arrayElements.add(objectToString(element));
            }
        }
        return OPEN_BRACKET + String.join(COMMA, arrayElements) + CLOSE_BRACKET;
    }

    private String collectionsToJson(Collection<?> value) {
        return OPEN_BRACKET + value.stream()
                .map(this::objectToString)
                .collect(Collectors.joining(COMMA)) + CLOSE_BRACKET;
    }

    private String mapToJson(Map<?, ?> value) {
        return OPEN_BRACE + value.entrySet().stream()
                .map(entry -> QUOTE + entry.getKey() + QUOTE + COLON + objectToString(entry.getValue()))
                .collect(Collectors.joining(COMMA)) + CLOSE_BRACE;
    }

    private boolean isPrimitiveOrWrapper(Object object) {
        return object instanceof Integer || object instanceof Double ||
                object instanceof Float || object instanceof Boolean ||
                object instanceof Character || object instanceof Byte ||
                object instanceof Short || object instanceof Long;
    }

    public <T> T deserialize(String json,
                             Class<T> clazz) throws Exception {
        JsonParser jsonParser = new JsonParser(new JsonTokenizer().tokenize(json));
        Object parse = jsonParser.parse();
        Map<String, Object> objectMap = (Map<String, Object>) parse;

        for (final Map.Entry<String, Object> stringObjectEntry : objectMap.entrySet()) {
            System.out.println(stringObjectEntry);
        }
        T instance = clazz.getDeclaredConstructor().newInstance();
        for (Field declaredField : clazz.getDeclaredFields()) {
            System.out.println(declaredField.getType());
            declaredField.setAccessible(true);
            String name = declaredField.getName();
            Object value = objectMap.get(name);

            if (UUID.class.isAssignableFrom(declaredField.getType()) && value instanceof String) {
                declaredField.set(instance, UUID.fromString((String) value));
            }
            else if (OffsetDateTime.class.isAssignableFrom(declaredField.getType()) && value instanceof String) {
                OffsetDateTime dateTime = OffsetDateTime.parse((String) value);
                declaredField.set(instance, dateTime.withOffsetSameInstant(ZoneOffset.UTC));
            }
            else if (LocalDate.class.isAssignableFrom(declaredField.getType()) && value instanceof String) {
                declaredField.set(instance, LocalDate.parse((String) value));
            }
            else if (declaredField.getType().isArray() && value instanceof final List<?> list) {
                if (declaredField.getType().getComponentType().equals(String.class)) {
                    String[] array = list.toArray(new String[0]);
                    declaredField.set(instance, array);
                }
            }
            else if (Map.class.isAssignableFrom(declaredField.getType())) {
                Map<?, ?> map = (Map<?, ?>) value;
                Map<Object, Object> processedMap = new HashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object key = entry.getKey();
                    Object val = entry.getValue();
                    if (key instanceof String) {
                        try {
                            UUID uuid = UUID.fromString((String) key);
                            processedMap.put(uuid, val);
                        } catch (IllegalArgumentException e) {
                            processedMap.put(key, val);
                        }
                    }
                    else {
                        processedMap.put(key, val);
                    }
                }
                declaredField.set(instance, processedMap);
            }
            else {
                declaredField.set(instance, value);
            }
        }
        return instance;
    }
}