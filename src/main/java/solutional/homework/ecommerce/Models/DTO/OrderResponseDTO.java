package solutional.homework.ecommerce.Models.DTO;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@JsonPropertyOrder({"amount","id","products","status"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private UUID id;
    private String status;
    private Amount amount;
    private List<ProductDTO> products;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Amount {
        private String discount;
        private String paid;
        private String returns;
        private String total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDTO {
        private UUID id;
        private String name;
        private String price;
        private Long product_id;
        private int quantity;
        private UUID replacedWith;

        public ProductDTO(UUID id,Long product_id, String name, String price, int quantity) {
            this.id = id;
            this.product_id = product_id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
    }
}