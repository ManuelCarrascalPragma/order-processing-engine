package co.com.pragma.orderengine.infrastructure.driven_adapters.payment;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.ports.out.IChargeablePaymentGateway;
import co.com.pragma.orderengine.domain.ports.out.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StripePaymentAdapter implements IChargeablePaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentAdapter.class);
    private final String secretKey;

    public StripePaymentAdapter(@Value("${payment.stripe.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public PaymentResult charge(Money amount) {
        log.debug("Procesando cobro con pasarela Stripe en modo '{}'", resolveMode());
        return new PaymentResult(true, "ch_stripe_" + UUID.randomUUID().toString().substring(0, 8));
    }

    private String resolveMode() {
        return secretKey != null && secretKey.startsWith("sk_live") ? "live" : "test";
    }
}
