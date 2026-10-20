package co.com.pragma.orderengine.application.config;

import co.com.pragma.orderengine.domain.ports.in.ICheckoutOrderUseCase;
import co.com.pragma.orderengine.domain.ports.out.IChargeablePaymentGateway;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;
import co.com.pragma.orderengine.domain.usecases.CheckoutOrderUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderBeanConfiguration {

    @Bean
    public ICheckoutOrderUseCase checkoutOrderUseCase(
            IOrderRepositoryPort orderRepository,
            IChargeablePaymentGateway paymentGateway) {
        return new CheckoutOrderUseCaseImpl(orderRepository, paymentGateway);
    }
}
