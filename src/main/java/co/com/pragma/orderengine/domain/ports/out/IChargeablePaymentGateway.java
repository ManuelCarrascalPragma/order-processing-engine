package co.com.pragma.orderengine.domain.ports.out;

import co.com.pragma.orderengine.domain.model.Money;

public interface IChargeablePaymentGateway {
    PaymentResult charge(Money amount);
}
