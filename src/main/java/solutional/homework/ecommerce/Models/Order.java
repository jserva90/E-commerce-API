package solutional.homework.ecommerce.Models;

import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue
    private UUID id;
    private String status;

    @Embedded
    private Amount amount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<Product> products;
}

@Embeddable
@Data
@Table(name = "amounts")
class Amount {
    private String discount;
    private String paid;
    private String returns;
    private String total;
}