package solutional.homework.ecommerce.Models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "products")
public class Product {
    @Id
    private Integer id;
    private String name;
    private String price;
}