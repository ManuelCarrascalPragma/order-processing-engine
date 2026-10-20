package co.com.pragma.orderengine.infrastructure.entry_points.web.mapper;

import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.OrderStatus;
import co.com.pragma.orderengine.infrastructure.entry_points.web.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Currency;

@Mapper(componentModel = "spring")
public interface IOrderWebMapper {

    @Mapping(target = "amount", source = "totalAmount.amount")
    @Mapping(target = "currency", source = "totalAmount.currency", qualifiedByName = "currencyToCode")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    OrderResponse toResponse(Order order);

    @Named("currencyToCode")
    default String currencyToCode(Currency currency) {
        return currency == null ? null : currency.getCurrencyCode();
    }

    @Named("statusToString")
    default String statusToString(OrderStatus status) {
        return status == null ? null : status.name();
    }
}
