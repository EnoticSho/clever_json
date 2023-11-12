package clevertec;

import clevertec.entity.Token;
import clevertec.entity.TokenType;
import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonTokenizerTest {

    private JsonTokenizer tokenizer;


    @BeforeEach
    public void setUp() {
        tokenizer = new JsonTokenizer();
    }

    @Test
    void testValidJson() throws JsonParseException {
        //Given
        String json = "{\"name\": \"John\", \"age\": 30}";

        //When
        List<Token> tokens = tokenizer.tokenize(json);

        //Then
        assertEquals(9, tokens.size());
        assertEquals(TokenType.BEGIN_OBJECT, tokens.get(0).getType());
        assertEquals(TokenType.STRING, tokens.get(1).getType());
        assertEquals(TokenType.COLON, tokens.get(2).getType());
        assertEquals(TokenType.STRING, tokens.get(3).getType());
        assertEquals(TokenType.COMMA, tokens.get(4).getType());
        assertEquals(TokenType.STRING, tokens.get(5).getType());
        assertEquals(TokenType.COLON, tokens.get(6).getType());
        assertEquals(TokenType.NUMBER, tokens.get(7).getType());
        assertEquals(TokenType.END_OBJECT, tokens.get(8).getType());
    }

    @Test
    void testUnclosedString() {
        //Given
        JsonTokenizer tokenizer = new JsonTokenizer();
        String json = "{\"name\": \"John";
        String expectedMessage = "Unclosed string in JSON";

        //When
        Exception exception = assertThrows(JsonParseException.class, () -> {
            tokenizer.tokenize(json);
        });

        //Then
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    void testInvalidBooleanValue() {
        //Given
        JsonTokenizer tokenizer = new JsonTokenizer();
        String json = "{\"valid\": tru}";
        String expectedMessage = "Invalid value encountered";

        //When
        Exception exception = assertThrows(JsonParseException.class, () -> {
            tokenizer.tokenize(json);
        });

        //Then
        assertTrue(exception.getMessage().contains(expectedMessage));
    }
}
