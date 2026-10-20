package co.com.pragma.orderengine.domain.ports.out;

import co.com.pragma.orderengine.domain.model.Money;

public interface IRefundablePaymentGateway {
    PaymentResult refund(String transactionId, Money amount);
}
