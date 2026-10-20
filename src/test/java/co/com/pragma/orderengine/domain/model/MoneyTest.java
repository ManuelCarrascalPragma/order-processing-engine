package co.com.pragma.orderengine.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void shouldCreateUsdMoney() {
        Money money = Money.ofUsd(100.0);

        assertEquals(BigDecimal.valueOf(100.0), money.amount());
        assertEquals(USD, money.currency());
    }

    @Test
    void shouldExposeZeroConstant() {
        assertEquals(BigDecimal.ZERO, Money.ZERO.amount());
        assertEquals(USD, Money.ZERO.currency());
    }

    @Test
    void shouldAddTwoAmountsOfSameCurrency() {
        Money result = Money.ofUsd(50.0).add(Money.ofUsd(25.0));

        assertEquals(0, BigDecimal.valueOf(75.0).compareTo(result.amount()));
        assertEquals(USD, result.currency());
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(NullPointerException.class, () -> new Money(null, USD));
    }

    @Test
    void shouldRejectNullCurrency() {
        assertThrows(NullPointerException.class, () -> new Money(BigDecimal.TEN, null));
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> new Money(BigDecimal.valueOf(-1), USD));
    }

    @Test
    void shouldRejectAddingDifferentCurrencies() {
        Money usd = new Money(BigDecimal.TEN, USD);
        Money eur = new Money(BigDecimal.TEN, EUR);

        assertThrows(IllegalArgumentException.class, () -> usd.add(eur));
    }
}
