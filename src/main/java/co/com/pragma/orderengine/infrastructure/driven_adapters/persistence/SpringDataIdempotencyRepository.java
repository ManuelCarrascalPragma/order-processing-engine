package co.com.pragma.orderengine.infrastructure.driven_adapters.persistence;

import co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.entity.IdempotencyRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataIdempotencyRepository extends JpaRepository<IdempotencyRecordJpaEntity, String> {
}
