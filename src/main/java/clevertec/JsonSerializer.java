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
        else if (isPrimitiveOrWrapper(value.getClass())) {
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
            if (!isPrimitiveOrWrapper(element.getClass()) && !(element instanceof String)) {
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

    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.equals(Integer.class) || type.equals(Double.class) ||
                type.equals(Float.class) || type.equals(Boolean.class) ||
                type.equals(Character.class) || type.equals(Byte.class) ||
                type.equals(Short.class) || type.equals(Long.class);
    }

    public <T> T deserialize(String json,
                             Class<T> clazz) throws Exception {
        JsonParser jsonParser = new JsonParser(new JsonTokenizer().tokenize(json));
        Object parse = jsonParser.parse();
        Map<String, Object> objectMap = (Map<String, Object>) parse;

        T instance = clazz.getDeclaredConstructor().newInstance();
        for (Field declaredField : clazz.getDeclaredFields()) {
            declaredField.setAccessible(true);
            String name = declaredField.getName();
            Object value = objectMap.get(name);

            if (isSimpleField(declaredField.getType())) {
                declaredField.set(instance, convertSimpleField(value, declaredField.getType()));
            }
            else if (declaredField.getType().isArray() && value instanceof final List<?> list) {
                declaredField.set(instance, deserializeArray(declaredField.getType().getComponentType(), list));
            }
            else if (List.class.isAssignableFrom(declaredField.getType())) {
                List<?> list = (List<?>) value;
                List<Object> processedList = new ArrayList<>();
                for (final Object o : list) {
                    processedList.add(deserializeItem(o, getListGenericType(declaredField)));
                }
                declaredField.set(instance, processedList);
            }
            else if (Map.class.isAssignableFrom(declaredField.getType())) {
                Map<?, ?> map = (Map<?, ?>) value;
                Map<Object, Object> processedMap = new HashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object key = deserializeItem(entry.getKey(), getKeyMapValueType(declaredField));
                    Object val = deserializeItem(entry.getValue(), getValueMapValueType(declaredField));
                    processedMap.put(key, val);
                }
                declaredField.set(instance, processedMap);
            }
            else {
                declaredField.set(instance, deserializeItem(value, declaredField.getType()));
            }
        }
        return instance;
    }

    private Object deserializeItem(Object item, Type type) throws Exception {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();

            if (rawType.equals(List.class)) {
                Type itemType = parameterizedType.getActualTypeArguments()[0];
                List<Object> list = new ArrayList<>();
                for (Object o : (List<?>) item) {
                    list.add(deserializeItem(o, itemType));
                }
                return list;
            }
            if (rawType.equals(Map.class)) {
                Type keyType = parameterizedType.getActualTypeArguments()[0];
                Type valueType = parameterizedType.getActualTypeArguments()[1];
                Map<Object, Object> map = new HashMap<>();
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet()) {
                    Object key = convertSimpleField(entry.getKey(), (Class<?>) keyType);
                    Object value = deserializeItem(entry.getValue(), valueType);
                    map.put(key, value);
                }
                return map;
            }
        }

        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (isSimpleField(clazz)) {
                return convertSimpleField(item, clazz);
            }
            else {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Map<?, ?> objectMap = (Map<?, ?>) item;
                    Object value = objectMap.get(fieldName);

                    if (field.getType().equals(List.class) || field.getType().equals(Map.class)) {
                        Type fieldType = field.getGenericType();
                        field.set(instance, deserializeItem(value, fieldType));
                    }
                    else if (field.getType().isArray() && value instanceof final List<?> list) {
                        field.set(instance, deserializeArray(field.getType().getComponentType(), list));
                    }
                    else {
                        field.set(instance, convertSimpleField(value, field.getType()));
                    }
                }
                return instance;
            }
        }
        return null;
    }

    private boolean isSimpleField(Class<?> type) {
        return isPrimitiveOrWrapper(type) || type.equals(String.class) ||
                type.equals(UUID.class) || type.equals(OffsetDateTime.class) ||
                type.equals(LocalDate.class);
    }

    private Object convertSimpleField(Object value, Class<?> type) {
        if (UUID.class.isAssignableFrom(type)) {
            return UUID.fromString((String) value);
        }
        else if (String.class.isAssignableFrom(type)) {
            return value;
        }
        else if (LocalDate.class.isAssignableFrom(type)) {
            return LocalDate.parse((String) value);
        }
        else if (OffsetDateTime.class.isAssignableFrom(type)) {
            OffsetDateTime dateTime = OffsetDateTime.parse((String) value);
            return dateTime.withOffsetSameInstant(ZoneOffset.UTC);
        }
        else if (Double.class.isAssignableFrom(type)) {
            return Double.parseDouble(String.valueOf(value));
        }
        else if (Long.class.isAssignableFrom(type)) {
            return Long.parseLong(String.valueOf(value));
        }
        else if (Integer.class.isAssignableFrom(type)) {
            return Integer.parseInt(String.valueOf(value));
        }
        else if (Boolean.class.isAssignableFrom(type)) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return value;
    }

    private Object deserializeArray(Class<?> componentType, List<?> list) {
        if (componentType.equals(String.class)) {
            return list.toArray(new String[0]);
        } else if (componentType.equals(Double.class)) {
            return list.toArray(new Double[0]);
        } else if (componentType.equals(Long.class)) {
            return list.toArray(new Long[0]);
        } else if (componentType.equals(Float.class)) {
            return list.toArray(new Float[0]);
        } else if (componentType.equals(Byte.class)) {
            return list.toArray(new Byte[0]);
        }
        else return list.toArray(new Integer[0]);
    }


    private Type getListGenericType(Field field) {
        Type genericFieldType = field.getGenericType();
        if (genericFieldType instanceof final ParameterizedType parameterizedType) {
            return parameterizedType.getActualTypeArguments()[0];
        }
        return Object.class;
    }

    private Type getValueMapValueType(Field field) {
        Type genericFieldType = field.getGenericType();
        if (genericFieldType instanceof final ParameterizedType parameterizedType) {
            return parameterizedType.getActualTypeArguments()[1];
        }
        return Object.class;
    }

    private Type getKeyMapValueType(Field field) {
        Type genericFieldType = field.getGenericType();
        if (genericFieldType instanceof final ParameterizedType parameterizedType) {
            return parameterizedType.getActualTypeArguments()[0];
        }
        return Object.class;
    }
}