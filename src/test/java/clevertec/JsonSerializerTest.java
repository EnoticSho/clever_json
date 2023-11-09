package clevertec;

import clevertec.entity.Customer;
import clevertec.entity.Order;
import clevertec.entity.Product;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonSerializerTest {

    private JsonSerializer serializer;
    private ObjectMapper jacksonMapper;

    @BeforeEach
    public void setUp() {
        serializer = new JsonSerializer();
        jacksonMapper = new ObjectMapper();
        jacksonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        jacksonMapper.registerModule(new JavaTimeModule());
        jacksonMapper.configOverride(OffsetDateTime.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"));
    }

    @Test
    public void testCustomerSerialization() throws Exception {
        //Given
        Customer customer = Customer.builder().build();

        // When
        String jsonSerializer = serializer.serialize(customer);
        String jacksonJson = jacksonMapper.writeValueAsString(customer);
        System.out.println(jacksonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(customer));

        // Then
        assertEquals(jacksonJson, jsonSerializer);
    }

    @Test
    public void testOrderSerialization() throws Exception {
        // Given
        Order order = Order.builder().build();

        // When
        String jsonSerializer = serializer.serialize(order);
        String jacksonJson = jacksonMapper.writeValueAsString(order);

        // Then
        assertEquals(jacksonJson, jsonSerializer);
    }

    @Test
    public void testProductSerialization() throws Exception {
        // Given
        Product product = Product.builder()
                .build();

        // When
        String jsonSerializer = serializer.serialize(product);
        String jacksonJson = jacksonMapper.writeValueAsString(product);

        // Then
        assertEquals(jacksonJson, jsonSerializer);
    }
}

