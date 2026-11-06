package co.com.pragma.orderengine.infrastructure.driven_adapters.persistence;

import co.com.pragma.orderengine.domain.model.IdempotencyRecord;
import co.com.pragma.orderengine.domain.ports.out.IIdempotencyStorePort;
import co.com.pragma.orderengine.infrastructure.driven_adapters.persistence.entity.IdempotencyRecordJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class IdempotencyStoreAdapter implements IIdempotencyStorePort {

    private final SpringDataIdempotencyRepository repository;

    public IdempotencyStoreAdapter(SpringDataIdempotencyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<IdempotencyRecord> findByKey(String key) {
        return repository.findById(key).map(this::toDomain);
    }

    @Override
    public IdempotencyRecord save(IdempotencyRecord record) {
        IdempotencyRecordJpaEntity entity = new IdempotencyRecordJpaEntity(
                record.key(), record.statusCode(), record.responseBody());
        repository.save(entity);
        return record;
    }

    private IdempotencyRecord toDomain(IdempotencyRecordJpaEntity entity) {
        return new IdempotencyRecord(entity.getKey(), entity.getStatusCode(), entity.getResponseBody());
    }
}
