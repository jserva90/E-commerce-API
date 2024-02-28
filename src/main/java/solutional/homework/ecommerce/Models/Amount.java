package solutional.homework.ecommerce.Models;

import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;


@Data
@Table(name = "amounts")
public
class Amount {
    private String discount;
    private String paid;
    private String returns;
    private String total;

    public Amount(BigDecimal zero, BigDecimal zero1, BigDecimal zero2, BigDecimal zero3) {
    }
}
