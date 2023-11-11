package clevertec;

import clevertec.entity.Customer;
import clevertec.entity.Order;
import clevertec.entity.Product;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
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

        System.out.println(jsonSerializer);

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
        System.out.println(jsonSerializer);

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
        System.out.println(jsonSerializer);

        // Then
        assertEquals(jacksonJson, jsonSerializer);
    }

    @Test
    public void testDeserializationProduct() throws Exception {
        String json = "{\"id\":\"567ab0a2-4642-4b56-9720-ec8c17ffbb94\",\"name\":\"Mercedes\",\"price\":300.25,\"reviews\":[\"Excellent\",\"Good\"],\"categories\":[\"Cars\",\"Mercedes\"],\"specifications\":{\"Weight\":[\"1kg\",\"1.5kg\",\"2kg\"],\"Color\":[\"Red\",\"Blue\",\"Green\"]},\"userReviews\":{\"25a4b739-d0cf-4211-94d7-5a86b0264870\":[\"bad\",\"very bad\"],\"567ab0a2-4642-4b56-9720-ec8c17ffbb94\":[\"good\",\"very good\"]}}";

        Product actual = serializer.deserialize(json, Product.class);
        Product expected = jacksonMapper.readValue(json, Product.class);

        assertThat(actual)
                .hasFieldOrPropertyWithValue(Product.Fields.id, expected.getId())
                .hasFieldOrPropertyWithValue(Product.Fields.name, expected.getName())
                .hasFieldOrPropertyWithValue(Product.Fields.price, expected.getPrice())
                .hasFieldOrPropertyWithValue(Product.Fields.categories, expected.getCategories())
                .hasFieldOrPropertyWithValue(Product.Fields.reviews, expected.getReviews())
                .hasFieldOrPropertyWithValue(Product.Fields.userReviews, expected.getUserReviews());
    }

    @Test
    public void testOrderDeserialization() throws Exception {
        String json = "{\"id\":\"a0944f24-873d-4b35-8355-6121647e2f87\",\"products\":[{\"id\":\"567ab0a2-4642-4b56-9720-ec8c17ffbb94\",\"name\":\"Mercedes\",\"price\":300.25,\"reviews\":[\"Excellent\",\"Good\"],\"categories\":[\"Cars\",\"Mercedes\"],\"specifications\":{\"Weight\":[\"1kg\",\"1.5kg\",\"2kg\"],\"Color\":[\"Red\",\"Blue\",\"Green\"]},\"userReviews\":{\"25a4b739-d0cf-4211-94d7-5a86b0264870\":[\"bad\",\"very bad\"],\"567ab0a2-4642-4b56-9720-ec8c17ffbb94\":[\"good\",\"very good\"]}},{\"id\":\"a464c376-910c-4cad-8abe-2fb6efe94c79\",\"name\":\"Porsche\",\"price\":300.25,\"reviews\":[\"Excellent\",\"Good\"],\"categories\":[\"Cars\",\"Mercedes\"],\"specifications\":{\"Weight\":[\"1kg\",\"1.5kg\",\"2kg\"],\"Color\":[\"Red\",\"Blue\",\"Green\"]},\"userReviews\":{\"25a4b739-d0cf-4211-94d7-5a86b0264870\":[\"bad\",\"very bad\"],\"567ab0a2-4642-4b56-9720-ec8c17ffbb94\":[\"good\",\"very good\"]}}],\"createDate\":\"2023-11-09T15:38:45.744420+02:00\"}";

        Order actual = serializer.deserialize(json, Order.class);
        Order expected = jacksonMapper.readValue(json, Order.class);

        assertThat(actual)
                .hasFieldOrPropertyWithValue(Order.Fields.id, expected.getId())
                .hasFieldOrPropertyWithValue(Order.Fields.products, expected.getProducts())
                .hasFieldOrPropertyWithValue(Order.Fields.createDate, expected.getCreateDate());
    }

    @Test
    public void testCustomerDeserialization() throws Exception {
        String json = "{\"id\":\"41548c34-6471-4ce6-9512-644943b68d10\",\"firstName\":\"Sergey\",\"lastName\":\"Turpakov\",\"dateBirth\":\"1999-02-08\",\"orders\":[{\"id\":\"a0944f24-873d-4b35-8355-6121647e2f87\",\"products\":[{\"id\":\"567ab0a2-4642-4b56-9720-ec8c17ffbb94\",\"name\":\"Mercedes\",\"price\":300.25,\"reviews\":[\"Excellent\",\"Good\"],\"categories\":[\"Cars\",\"Mercedes\"],\"specifications\":{\"Color\":[\"Red\",\"Blue\",\"Green\"],\"Weight\":[\"1kg\",\"1.5kg\",\"2kg\"]},\"userReviews\":{\"25a4b739-d0cf-4211-94d7-5a86b0264870\":[\"bad\",\"very bad\"],\"567ab0a2-4642-4b56-9720-ec8c17ffbb94\":[\"good\",\"very good\"]}},{\"id\":\"a464c376-910c-4cad-8abe-2fb6efe94c79\",\"name\":\"Porsche\",\"price\":300.25,\"reviews\":[\"Excellent\",\"Good\"],\"categories\":[\"Cars\",\"Mercedes\"],\"specifications\":{\"Color\":[\"Red\",\"Blue\",\"Green\"],\"Weight\":[\"1kg\",\"1.5kg\",\"2kg\"]},\"userReviews\":{\"25a4b739-d0cf-4211-94d7-5a86b0264870\":[\"bad\",\"very bad\"],\"567ab0a2-4642-4b56-9720-ec8c17ffbb94\":[\"good\",\"very good\"]}}],\"createDate\":\"2023-11-09T15:38:45.744420+02:00\"}],\"preferences\":{\"buys most often\":[{\"id\":\"567ab0a2-4642-4b56-9720-ec8c17ffbb94\",\"name\":\"Mercedes\",\"price\":300.25,\"reviews\":[\"Excellent\",\"Good\"],\"categories\":[\"Cars\",\"Mercedes\"],\"specifications\":{\"Color\":[\"Red\",\"Blue\",\"Green\"],\"Weight\":[\"1kg\",\"1.5kg\",\"2kg\"]},\"userReviews\":{\"25a4b739-d0cf-4211-94d7-5a86b0264870\":[\"bad\",\"very bad\"],\"567ab0a2-4642-4b56-9720-ec8c17ffbb94\":[\"good\",\"very good\"]}}]}}";

        Customer actual = serializer.deserialize(json, Customer.class);
        Customer expected = jacksonMapper.readValue(json, Customer.class);

        assertThat(actual)
                .hasFieldOrPropertyWithValue(Customer.Fields.firstName, expected.getFirstName())
                .hasFieldOrPropertyWithValue(Customer.Fields.lastName, expected.getLastName())
                .hasFieldOrPropertyWithValue(Customer.Fields.dateBirth, expected.getDateBirth())
                .hasFieldOrPropertyWithValue(Customer.Fields.preferences, expected.getPreferences())
                .hasFieldOrPropertyWithValue(Customer.Fields.orders, expected.getOrders());
    }
}

