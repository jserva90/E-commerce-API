package solutional.homework.ecommerce.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class BigDecimalToStringConverter {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public static String convert(BigDecimal value) {
        return DECIMAL_FORMAT.format(value);
    }
}
