package co.com.pragma.orderengine.infrastructure.driven_adapters.payment;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.ports.out.IChargeablePaymentGateway;
import co.com.pragma.orderengine.domain.ports.out.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StripePaymentAdapter implements IChargeablePaymentGateway {
    @Override
    public PaymentResult charge(Money amount) {
        return new PaymentResult(true, "ch_stripe_" + UUID.randomUUID().toString().substring(0, 8));
    }
}
