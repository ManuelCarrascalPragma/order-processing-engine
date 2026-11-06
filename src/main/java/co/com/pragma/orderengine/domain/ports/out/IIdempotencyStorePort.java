package co.com.pragma.orderengine.domain.ports.out;

import co.com.pragma.orderengine.domain.model.IdempotencyRecord;

import java.util.Optional;

public interface IIdempotencyStorePort {

    Optional<IdempotencyRecord> findByKey(String key);

    IdempotencyRecord save(IdempotencyRecord record);
}
