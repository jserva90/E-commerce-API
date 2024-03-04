package solutional.homework.ecommerce.Models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private String status;

    @Column(precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal paid = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal returns = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private Set<OrderItem> items = new HashSet<>();

    public void addProduct(Product product,int quantity){
        OrderItem orderItem = new OrderItem(this,product,quantity);
        items.add(orderItem);
    }

    public void calculateTotal(){
        BigDecimal newTotal = items.stream()
                .filter(item -> !item.isReplaced()) // Exclude replaced items
                .map(item -> new BigDecimal(item.getProduct().getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(this.discount)
                .add(this.returns);
        this.total = newTotal;
    }

    public void updateAmountsBasedOnReplacements() {
        BigDecimal originalTotal = this.paid;
        BigDecimal accumulatedDifference = BigDecimal.ZERO;

        for (OrderItem item : this.items) {
            if (item.getReplacedWith() != null) {
                BigDecimal itemTotalPrice = new BigDecimal(item.getProduct().getPrice()).multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal replacementTotalPrice = new BigDecimal(item.getReplacedWith().getProduct().getPrice())
                        .multiply(BigDecimal.valueOf(item.getReplacedWith().getQuantity()));
                BigDecimal priceDifference = replacementTotalPrice.subtract(itemTotalPrice);
                accumulatedDifference = accumulatedDifference.add(priceDifference);
            }
        }

        if (accumulatedDifference.compareTo(BigDecimal.ZERO) > 0) {
            this.discount = accumulatedDifference;
            this.returns = BigDecimal.ZERO;
        } else {
            this.returns = accumulatedDifference.abs();
            this.discount = BigDecimal.ZERO;
        }

        this.total = originalTotal.subtract(returns);
    }
}

