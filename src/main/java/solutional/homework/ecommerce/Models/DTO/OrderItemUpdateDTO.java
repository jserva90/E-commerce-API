package solutional.homework.ecommerce.Models.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemUpdateDTO {
    private Integer quantity;

    @JsonProperty("replaced_with")
    private Replacement replacedWith;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Replacement {
        @JsonProperty("product_id")
        private Long productId;
        private Integer quantity;
    }
}