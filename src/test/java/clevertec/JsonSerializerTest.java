package clevertec;

import clevertec.entity.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonSerializerTest {

    private final ObjectMapper jacksonMapper = new ObjectMapper();
    private final JsonSerializer jsonSerializer = new JsonSerializer();

    @Test
    public void whenSerializingProduct_thenCorrect() throws Exception {
        Product product = new Product(UUID.fromString("17e58185-daae-4c7a-a7a0-fded5981690d"), "Product1", 10.99);
        String serialize = jsonSerializer.serialize(product);
        String jacksonSerialized = jacksonMapper.writeValueAsString(product);

        assertEquals(jacksonSerialized, serialize);
    }

    @Test
    public void whenSerializingProductWithNulls_thenCorrect() throws Exception {
        Product product = new Product(null, "Product2", null);
        String serialize = jsonSerializer.serialize(product);
        String jacksonSerialized = jacksonMapper.writeValueAsString(product);

        assertEquals(jacksonSerialized, serialize);
    }

    @Test
    public void whenSerializingProductWithZeroPrice_thenCorrect() throws Exception {
        Product product = new Product(UUID.fromString("17e58185-daae-4c7a-a7a0-fded5981690d"), "Product3", 0.0);
        String serialize = jsonSerializer.serialize(product);
        String jacksonSerialized = jacksonMapper.writeValueAsString(product);

        assertEquals(jacksonSerialized, serialize);
    }
}
