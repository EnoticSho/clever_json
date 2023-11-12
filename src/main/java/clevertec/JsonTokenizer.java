package clevertec;

import clevertec.entity.Token;
import clevertec.entity.TokenType;
import com.fasterxml.jackson.core.JsonParseException;

import java.util.ArrayList;
import java.util.List;

public class JsonTokenizer {

    public List<Token> tokenize(String json) throws JsonParseException {
        List<Token> tokens = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            if (Character.isWhitespace(json.charAt(i)))
                continue;
            switch (json.charAt(i)) {
                case '{' -> tokens.add(new Token(TokenType.BEGIN_OBJECT, "{"));
                case '}' -> tokens.add(new Token(TokenType.END_OBJECT, "}"));
                case '[' -> tokens.add(new Token(TokenType.BEGIN_ARRAY, "["));
                case ']' -> tokens.add(new Token(TokenType.END_ARRAY, "]"));
                case ':' -> tokens.add(new Token(TokenType.COLON, ":"));
                case ',' -> tokens.add(new Token(TokenType.COMMA, ","));
                case '\"' -> {
                    i++;
                    stringBuilder.setLength(0);
                    while (i < json.length() && (json.charAt(i) != '\"' || json.charAt(i - 1) == '\\')) {
                        stringBuilder.append(json.charAt(i));
                        i++;
                    }
                    if (i >= json.length()) {
                        throw new JsonParseException("Unclosed string in JSON at position: " + (i - stringBuilder.length()));
                    }
                    tokens.add(new Token(TokenType.STRING, stringBuilder.toString()));
                }
                case 't', 'f', 'n' -> {
                    stringBuilder.setLength(0);
                    while (i < json.length() && json.charAt(i) != ',' && json.charAt(i) != ']' && json.charAt(i) != '}') {
                        stringBuilder.append(json.charAt(i));
                        i++;
                    }
                    String value = stringBuilder.toString();
                    if (!value.equals("true") && !value.equals("false") && !value.equals("null")) {
                        throw new JsonParseException("Invalid value encountered in JSON at position: " + (i - value.length()) + ": " + value);
                    }
                    i--;
                    tokens.add(new Token(value.equals("null") ? TokenType.NULL : TokenType.BOOLEAN, value));
                }
                default -> {
                    if (Character.isDigit(json.charAt(i))) {
                        stringBuilder.setLength(0);
                        stringBuilder.append(json.charAt(i));
                        i++;
                        while (i < json.length()) {
                            if (Character.isDigit(json.charAt(i)) || json.charAt(i) == '.' || json.charAt(i) == 'e' || json.charAt(i) == 'E' || json.charAt(i) == '+' || json.charAt(i) == '-') {
                                stringBuilder.append(json.charAt(i));
                                i++;
                            }
                            else {
                                break;
                            }
                        }
                        i--;
                        tokens.add(new Token(TokenType.NUMBER, stringBuilder.toString()));
                    }
                }
            }
        }
        return tokens;
    }
}
