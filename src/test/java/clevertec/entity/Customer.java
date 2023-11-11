package clevertec.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@Builder(setterPrefix = "with")
public class Customer {

    @Builder.Default
    private UUID id = UUID.fromString("41548c34-6471-4ce6-9512-644943b68d10");

    @Builder.Default
    private String firstName = "Sergey";

    @Builder.Default
    private String lastName = "Turpakov";

    @Builder.Default
    private LocalDate dateBirth = LocalDate.of(1999, 2, 8);

    @Builder.Default
    private List<Order> orders = List.of(Order.builder().build());

    @Builder.Default
    private Map<String, List<Product>> preferences = Map.of(
            "buys most often", List.of(Product.builder().build())
    );
}
