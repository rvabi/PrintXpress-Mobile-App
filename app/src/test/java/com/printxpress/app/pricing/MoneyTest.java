package com.printxpress.app.pricing;

import static org.junit.Assert.assertEquals;
import java.math.BigDecimal;
import org.junit.Test;

public class MoneyTest {
    @Test public void roundsHalfUpToTwoDecimals() {
        assertEquals(new BigDecimal("1500.01"), Money.amount("1500.005"));
        assertEquals(new BigDecimal("2.68"), Money.amount("2.675"));
    }

    @Test public void formatsSriLankanRupeesWithoutChangingTheAmount() {
        assertEquals("LKR 1,500.00", Money.lkr(Money.amount("1500")));
    }
}
