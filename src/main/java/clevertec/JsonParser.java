package clevertec;

import clevertec.entity.Token;
import clevertec.entity.TokenType;
import clevertec.exceptions.JsonParserException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static clevertec.entity.TokenType.COLON;
import static clevertec.entity.TokenType.COMMA;
import static clevertec.entity.TokenType.END_ARRAY;
import static clevertec.entity.TokenType.END_OBJECT;
import static clevertec.entity.TokenType.STRING;

public class JsonParser {
    private final List<Token> tokenList;
    private int currentTokenIndex = 0;

    public JsonParser(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public Object parse() {
        if (tokenList.isEmpty()) {
            throw new RuntimeException("empty list");
        }
        return parseValue();
    }

    private Object parseValue() {
        Token token = getToken();
        switch (token.getType()) {
            case BEGIN_OBJECT -> {
                return parseObject();
            }
            case BEGIN_ARRAY -> {
                return parseArray();
            }
            case STRING -> {
                return token.getValue();
            }
            case NULL -> {
                return null;
            }
            case NUMBER -> {
                return parseNumber(token);
            }
            case BOOLEAN -> {
                return Boolean.parseBoolean(token.getValue());
            }
        }
        return null;
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> objectMap = new HashMap<>();
        Token token = peekNextToken();
        while (token.getType() != END_OBJECT) {
            token = peekNextToken();
            expectedToken(STRING);
            String key = token.getValue();
            expectedToken(COLON);
            Object value = parseValue();
            objectMap.put(key, value);
            token = peekNextToken();

            if (token.getType() != END_OBJECT) {
                expectedToken(COMMA);
            }
        }
        expectedToken(END_OBJECT);
        return objectMap;
    }

    private List<Object> parseArray() {
        List<Object> array = new ArrayList<>();

        Token token = peekNextToken();
        while (token.getType() != END_ARRAY) {
            array.add(parseValue());
            token = peekNextToken();
            if (token.getType() != END_ARRAY) {
                expectedToken(COMMA);
            }
        }
        expectedToken(END_ARRAY);
        return array;
    }

    private Object parseNumber(Token token) {
        try {
            if (token.getValue().contains(".")) {
                return Double.parseDouble(token.getValue());
            }
            else
                return Long.parseLong(token.getValue());
        } catch (NumberFormatException e) {
            throw new JsonParserException("Invalid number " + (currentTokenIndex - 1));
        }
    }

    private Token getToken() {
        return tokenList.get(currentTokenIndex++);
    }

    private Token peekNextToken() {
        return tokenList.get(currentTokenIndex);
    }

    private void expectedToken(TokenType expectedTokenType) {
        Token token = getToken();
        if (token.getType() != expectedTokenType) {
            throw new JsonParserException("Expected " + expectedTokenType + " but was: " + token.getType() + " Position" + (currentTokenIndex - 1));
        }
    }
}
