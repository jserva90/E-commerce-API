package solutional.homework.ecommerce.Models.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemReplacementDTO {

    private Replacement replaced_with;

    public OrderItemReplacementDTO(@JsonProperty("replaced_with") Replacement replaced_with) {
        this.replaced_with = replaced_with;
    }

    @Getter
    @Setter
    public static class Replacement {
        private Long product_id;
        private int quantity;

        public Replacement() {}

        public Replacement(Long product_id, int quantity) {
            this.product_id = product_id;
            this.quantity = quantity;
        }
    }
}
