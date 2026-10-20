package co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.mapper;

import co.com.pragma.orderengine.domain.model.Money;
import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.entity.OrderJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.Currency;

@Mapper(componentModel = "spring")
public interface IOrderPersistenceMapper {

    @Mapping(target = "amount", source = "totalAmount.amount")
    @Mapping(target = "currency", source = "totalAmount.currency", qualifiedByName = "currencyToCode")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    OrderJpaEntity toEntity(Order order);

    default Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Order(
                entity.getId(),
                entity.getCustomerEmail(),
                new Money(entity.getAmount(), Currency.getInstance(entity.getCurrency())),
                OrderStatus.valueOf(entity.getStatus()),
                entity.getTransactionId()
        );
    }

    @Named("currencyToCode")
    default String currencyToCode(Currency currency) {
        return currency == null ? null : currency.getCurrencyCode();
    }

    @Named("statusToString")
    default String statusToString(OrderStatus status) {
        return status == null ? null : status.name();
    }
}
