package clevertec.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@Builder(setterPrefix = "with")
public class Product {

    @Builder.Default
    private UUID id = UUID.fromString("567ab0a2-4642-4b56-9720-ec8c17ffbb94");

    @Builder.Default
    private String name = "Mercedes";

    @Builder.Default
    private Double price = 300.25;

    @Builder.Default
    private String[] reviews = new String[]{"Excellent", "Good"};

    @Builder.Default
    private List<String> categories = List.of("Cars", "Mercedes");

    @Builder.Default
    private Map<String, List<String>> specifications = Map.of(
            "Color", List.of("Red", "Blue", "Green"),
            "Weight", List.of("1kg", "1.5kg", "2kg")
    );

    @Builder.Default
    private Map<UUID, List<String>> userReviews = Map.of(
            UUID.fromString("567ab0a2-4642-4b56-9720-ec8c17ffbb94"), List.of("good", "very good"),
            UUID.fromString("25a4b739-d0cf-4211-94d7-5a86b0264870"), List.of("bad", "very bad")
    );
}
