package co.com.pragma.orderengine.infrastructure.driven_adapters.payment;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.ports.out.PaymentResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StripePaymentAdapterTest {

    private final StripePaymentAdapter adapter = new StripePaymentAdapter();

    @Test
    void shouldReturnSuccessfulPaymentResult() {
        PaymentResult result = adapter.charge(Money.ofUsd(100.0));

        assertTrue(result.isSuccess());
        assertNotNull(result.transactionId());
        assertTrue(result.transactionId().startsWith("ch_stripe_"));
    }
}
