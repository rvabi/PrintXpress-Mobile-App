package com.printxpress.app.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class Money {
    private Money() { }

    public static BigDecimal amount(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    public static String lkr(BigDecimal amount) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "LKR " + format.format(amount.setScale(2, RoundingMode.HALF_UP));
    }
}
