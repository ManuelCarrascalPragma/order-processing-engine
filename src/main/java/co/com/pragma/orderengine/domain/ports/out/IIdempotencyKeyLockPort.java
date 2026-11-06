package co.com.pragma.orderengine.domain.ports.out;

public interface IIdempotencyKeyLockPort {

    boolean tryAcquire(String key);

    void release(String key);
}
