package co.com.pragma.orderengine.infrastructure.driven_adapters.persistence;

import co.com.pragma.orderengine.domain.model.Order;
import co.com.pragma.orderengine.domain.model.exception.VersionConflictException;
import co.com.pragma.orderengine.domain.ports.out.IOrderRepositoryPort;
import co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.entity.OrderJpaEntity;
import co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.mapper.IOrderPersistenceMapper;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PostgresOrderRepositoryAdapter implements IOrderRepositoryPort {

    private final SpringDataJpaOrderRepository jpaRepository;
    private final IOrderPersistenceMapper mapper;

    public PostgresOrderRepositoryAdapter(SpringDataJpaOrderRepository jpaRepository,
                                          IOrderPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toEntity(order);
        OrderJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Order update(Order order) {
        OrderJpaEntity entity = mapper.toEntity(order);
        try {
            OrderJpaEntity saved = jpaRepository.saveAndFlush(entity);
            return mapper.toDomain(saved);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new VersionConflictException(
                    "Optimistic lock failed updating order " + order.getId());
        }
    }

    @Override
    public Optional<Order> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
