package clevertec.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Order {

    @Builder.Default
    private UUID id = UUID.fromString("a0944f24-873d-4b35-8355-6121647e2f87");

    @Builder.Default
    private Product[] products = new Product[]{
            Product.builder()
                    .build(),
            Product.builder()
                    .withId(UUID.fromString("a464c376-910c-4cad-8abe-2fb6efe94c79"))
                    .withName("Porsche")
                    .build()};

    @Builder.Default
    private OffsetDateTime createDate = OffsetDateTime.parse("2023-11-09T15:38:45.744420+02:00");
}
